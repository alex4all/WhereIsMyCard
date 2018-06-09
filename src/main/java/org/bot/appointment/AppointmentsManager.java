package org.bot.appointment;

import org.bot.cache.JedisCache;
import org.bot.net.ServerAPI;
import org.bot.utils.Utils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AppointmentsManager {
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final AppointmentsManager INSTANCE = new AppointmentsManager();


    public static final int DAYS_TO_SCAN = 180;
    private static final long UPDATE_PERIOD = 30 * 60 * 1000;
    private static final long REQUEST_DELAY = 2500;
    private static final int MAX_AVAILABLE_DATES = 10;
    private static final int EXPIRATION_TIME_SEC = 60 * 60;

    private final Map<AppointmentDate.Type, TreeMap<Long, AppointmentDate>> appointmentCache;
    private final Lock readLock;
    private final Lock writeLock;
    private final JedisCache jedisCache = JedisCache.getInstance();


    private AppointmentsManager() {
        appointmentCache = new HashMap<>();
        for (AppointmentDate.Type type : AppointmentDate.Type.values()) {
            appointmentCache.put(type, new TreeMap<>());
        }

        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
        preloadDataFromRedis();

        Timer timer = new Timer(true);
        TimerTask timerTask = new UpdateTask(DAYS_TO_SCAN);
        timer.schedule(timerTask, 0, UPDATE_PERIOD);
    }

    private void preloadDataFromRedis() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        Calendar calendar = Calendar.getInstance();
        try (Jedis jedis = jedisCache.getConnection()) {
            Pipeline pipe = jedis.pipelined();
            for (int i = 0; i < DAYS_TO_SCAN; i++) {
                String date = dateFormat.format(calendar.getTime());
                for (AppointmentDate.Type type : AppointmentDate.Type.values()) {
                    StringBuilder key = new StringBuilder("AppointmentDate")
                            .append(".").append(type.name())
                            .append(".").append(date);
                    //System.out.println("preload " + key);
                    pipe.hgetAll(key.toString());
                }
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            List<Object> result = pipe.syncAndReturnAll();
            for(Object object: result)
            {
                Map<String, String> map = (Map<String, String>) object;
                if(map.isEmpty())
                    continue;
                AppointmentDate date = new AppointmentDate(map);
                System.out.println("preloaded date: " + date.toString());
                try {
                    cacheNewDate(date, dateFormat);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } catch (JedisException e) {
            e.printStackTrace();
        }
    }

    public static AppointmentsManager getInstance() {
        return INSTANCE;
    }

    public List<AppointmentDate> getDateInfo(Date date) throws ParseException {
        return getDateInfo(date.getTime());
    }

    public Set<Long> getAvailableDates(AppointmentDate.Type type) {
        Set<Long> availableTime = new HashSet<>();
        readLock.lock();
        try {
            TreeMap<Long, AppointmentDate> map = appointmentCache.get(type);
            for (Entry<Long, AppointmentDate> entry : map.entrySet())
                if (entry.getValue().isAvailable())
                    availableTime.add(entry.getKey());
        } finally {
            readLock.unlock();
        }
        return availableTime;
    }

    public List<AppointmentDate> getDateInfo(String date) throws ParseException {
        return getDateInfo(Utils.dateToMills(date, new SimpleDateFormat(DATE_PATTERN)));
    }

    public List<AppointmentDate> getDateInfo(long dateAsMills) throws ParseException {
        List<AppointmentDate> dates = new ArrayList<>();
        readLock.lock();
        try {
            for (AppointmentDate.Type type : AppointmentDate.Type.values()) {
                dates.add(appointmentCache.get(type).get(dateAsMills));
            }
        } finally {
            readLock.unlock();
        }
        return dates;
    }

    public List<AppointmentDate> getFirstAvailableDates(AppointmentDate.Type type, int count) {
        List<AppointmentDate> dates = new ArrayList<>();
        if (count > MAX_AVAILABLE_DATES) {
            System.out.println(count + " is too big. Use default value: " + MAX_AVAILABLE_DATES);
            count = MAX_AVAILABLE_DATES;
        }
        if (count < 1) {
            System.out.println(count + " is not valid. Use min value: 1");
            count = 1;
        }
        readLock.lock();
        try {
            TreeMap<Long, AppointmentDate> datesMap = appointmentCache.get(type);
            for (Entry<Long, AppointmentDate> entry : datesMap.entrySet()) {
                if (entry.getValue().isAvailable())
                    dates.add(entry.getValue());
                if (dates.size() >= count)
                    return dates;
            }
            return dates;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Update data in cache. This is the only thread which performs write operations with cache. Don't need to do read lock here
     *
     * @param newDate
     * @throws ParseException
     */
    private void cacheNewDate(AppointmentDate newDate, DateFormat dateFormat) throws ParseException {
        System.out.println("Data updated: " + newDate.toString());
        Long newDateAsMills = Utils.dateToMills(newDate.getDate(), dateFormat);
        AppointmentDate.Type newType = newDate.getType();

        AppointmentDate oldDate = appointmentCache.get(newType).get(newDateAsMills);
        if (oldDate != null) {
            oldDate.update(newDate);
            return;
        }
        writeLock.lock();
        try {
            appointmentCache.get(newType).put(newDateAsMills, newDate);
        } finally {
            writeLock.unlock();
        }

        String key = new StringBuilder("AppointmentDate")
                .append(".").append(newDate.getType().name())
                .append(".").append(newDate.getDate()).toString();
        try (Jedis jedis = jedisCache.getConnection()) {
            jedis.hmset(key, newDate.getAsMap());
            jedis.expire(key, EXPIRATION_TIME_SEC);
        } catch (JedisException e) {
            e.printStackTrace();
        }
    }

    /**
     * Task to update cache info
     */
    private class UpdateTask extends TimerTask {
        private final DateFormat dateFormat;
        private final int daysToScan;

        UpdateTask(int daysToScan) {
            this.daysToScan = daysToScan;
            this.dateFormat = new SimpleDateFormat(DATE_PATTERN);
        }

        @Override
        public void run() {
            Calendar calendar = Calendar.getInstance();
            for (int i = 0; i < daysToScan; i++) {
                String date = dateFormat.format(calendar.getTime());
                for (AppointmentDate.Type type : AppointmentDate.Type.values()) {
                    try {
                        long updatedAt = System.currentTimeMillis();
                        AppointmentDate appointmentDate = ServerAPI.getDateInfo(date, type);
                        appointmentDate.setUpdatedAt(updatedAt);
                        cacheNewDate(appointmentDate, dateFormat);
                    } catch (IOException e) {
                        System.out.println("Exception occurred while sending request to urzad : " + e.getMessage());
                        e.printStackTrace();
                    } catch (ParseException e) {
                        System.out.println("Can't parse date : " + date + "; Error: " + e.getMessage());
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(REQUEST_DELAY);
                    } catch (InterruptedException e) {
                        System.out.println("Exception occurred while sleep: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            removeOldDatesFromCache();
        }

        /**
         * Remove expired information from cache. This is the only thread which performs write operations with cache. Don't need to do read lock here
         */
        private void removeOldDatesFromCache() {
            long currentTime = System.currentTimeMillis();
            String currentDate = Utils.millsToDate(currentTime, dateFormat);

            for (AppointmentDate.Type type : appointmentCache.keySet()) {
                Long firstKeyTime = appointmentCache.get(type).firstKey();
                String firstKeyDate = Utils.millsToDate(firstKeyTime, dateFormat);
                if (!currentDate.equals(firstKeyDate) && firstKeyTime < currentTime) {
                    writeLock.lock();
                    try {
                        appointmentCache.get(type).remove(firstKeyTime);
                    } finally {
                        writeLock.unlock();
                    }
                }
            }
        }
    }
}
