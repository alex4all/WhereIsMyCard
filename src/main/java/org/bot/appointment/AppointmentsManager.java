package org.bot.appointment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger log = LogManager.getLogger(AppointmentsManager.class);
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final AppointmentsManager INSTANCE = new AppointmentsManager();

    public static final int DAYS_TO_SCAN = 180;
    private static final long UPDATE_PERIOD = 20 * 60 * 1000;
    private static final long REQUEST_DELAY = 2500;
    private static final int MAX_AVAILABLE_DATES = 10;
    private static final int EXPIRATION_TIME_SEC = 60 * 60;

    private final Map<AppointmentDate.Type, TreeMap<Long, AppointmentDate>> availableAppointmentDates = new HashMap<>();
    //private final Map<Long, Long> scansHistoryMap = new HashMap<>();
    private final Lock readLock;
    private final Lock writeLock;
    private final JedisCache jedisCache = JedisCache.getInstance();

    private AppointmentsManager() {
        for (AppointmentDate.Type type : AppointmentDate.Type.values()) {
            availableAppointmentDates.put(type, new TreeMap<>());
        }

        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
        loadDataFromRedis();

        Timer timer = new Timer(true);
        TimerTask timerTask = new UpdateTask(DAYS_TO_SCAN);
        timer.schedule(timerTask, 0, 60 * 1000);
    }

    private void loadDataFromRedis() {
        log.info("Loading data from redis");
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        Calendar calendar = Calendar.getInstance();
        try (Jedis jedis = jedisCache.getConnection()) {
            Pipeline pipe = jedis.pipelined();
            for (int i = 0; i < DAYS_TO_SCAN; i++) {
                String date = dateFormat.format(calendar.getTime());
                for (AppointmentDate.Type type : AppointmentDate.Type.values()) {
                    String key = new StringBuilder("AppointmentDate")
                            .append(".").append(type.name())
                            .append(".").append(date)
                            .toString();
                    log.debug("Loading: " + key);
                    pipe.hgetAll(key);
                }
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            List<Object> result = pipe.syncAndReturnAll();
            int loaded = 0;
            int invalid = 0;
            for (Object object : result) {
                try {
                    Map<String, String> map = (Map<String, String>) object;
                    if (map.isEmpty()) {
                        invalid++;
                        continue;
                    }
                    AppointmentDate date = new AppointmentDate(map);
                    log.debug("Load date: " + date.toString());
                    loaded++;
                    updateLocalCache(date, dateFormat);
                } catch (ClassCastException e) {
                    log.error("Can't cast result from redis to Map<String, String>", e);
                }
            }
            log.info("Complete loading appointment dates info. Items loaded: " + loaded + "; invalid: " + invalid);
        } catch (JedisException e) {
            log.error(e);
        }
    }

    public static AppointmentsManager getInstance() {
        return INSTANCE;
    }

    public Set<Integer> getAvailableDays(Date month) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(month);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        long firstDate = calendar.getTimeInMillis();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        long lastDate = calendar.getTimeInMillis();
        Set<Long> timeSet = new HashSet<>();
        readLock.lock();
        try {
            for (TreeMap<Long, AppointmentDate> datesMap : availableAppointmentDates.values()) {
                timeSet.addAll(datesMap.navigableKeySet().subSet(firstDate, true, lastDate, true));
            }
        } finally {
            readLock.unlock();
        }

        Set<Integer> daysSet = new HashSet<>();
        for (Long time : timeSet) {
            calendar.setTimeInMillis(time);
            daysSet.add(calendar.get(Calendar.DAY_OF_MONTH));
        }
        log.info("Available days: " + daysSet.toString());
        return daysSet;
    }

    public Map<AppointmentDate.Type, AppointmentDate> getDateInfo(Date date) {
        return getDateInfo(date.getTime());
    }

    private Map<AppointmentDate.Type, AppointmentDate> getDateInfo(long dateAsMills) {
        Map<AppointmentDate.Type, AppointmentDate> dateInfo = new HashMap<>(AppointmentDate.Type.values().length);
        readLock.lock();
        try {
            for (AppointmentDate.Type type : AppointmentDate.Type.values()) {
                dateInfo.put(type, availableAppointmentDates.get(type).get(dateAsMills));
            }
        } finally {
            readLock.unlock();
        }
        return dateInfo;
    }

    public Date getAnyFirstAvailableDate() {
        long firstDate = Long.MAX_VALUE;
        readLock.lock();
        try {
            for (TreeMap<Long, AppointmentDate> datesMap : availableAppointmentDates.values()) {
                for (Entry<Long, AppointmentDate> entry : datesMap.entrySet()) {
                    if (entry.getValue().isAvailable()) {
                        long appointmentDate = entry.getKey();
                        firstDate = firstDate < appointmentDate ? firstDate : appointmentDate;
                        break;
                    }
                }
            }
        } finally {
            readLock.unlock();
        }
        return new Date(firstDate);
    }


    public AppointmentDate getFirstAvailableDates(AppointmentDate.Type type) {
        readLock.lock();
        try {
            TreeMap<Long, AppointmentDate> datesMap = availableAppointmentDates.get(type);
            if (datesMap.isEmpty())
                return null;
            return datesMap.get(datesMap.firstKey());
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Update data in local cache.
     *
     * @param updatedDateInfo updated information about date
     */
    private void updateLocalCache(AppointmentDate updatedDateInfo, DateFormat dateFormat) {
        Long dateKey;
        try {
            dateKey = Utils.dateToMills(updatedDateInfo.getDate(), dateFormat);
        } catch (ParseException e) {
            log.error("AppointmentDate is not cached. Can't parse date: " + updatedDateInfo.getDate(), e);
            throw new RuntimeException(e);
        }
        // remove appointment from available list if it is not available
        if (!updatedDateInfo.isAvailable()) {
            writeLock.lock();
            try {
                availableAppointmentDates.get(updatedDateInfo.getType()).remove(dateKey);
                return;
            } finally {
                writeLock.unlock();
            }
        }

        // update appointment if it is in the list
        AppointmentDate.Type type = updatedDateInfo.getType();
        readLock.lock();
        try {
            AppointmentDate oldDate = availableAppointmentDates.get(type).get(dateKey);
            if (oldDate != null) {
                oldDate.update(updatedDateInfo);
                return;
            }
        } finally {
            readLock.unlock();
        }

        // add appointment
        writeLock.lock();
        try {
            availableAppointmentDates.get(type).put(dateKey, updatedDateInfo);
        } finally {
            writeLock.unlock();
        }
    }

    private void cacheRemote(AppointmentDate appointmentDate) {
        String key = new StringBuilder("AppointmentDate")
                .append(".").append(appointmentDate.getType().name())
                .append(".").append(appointmentDate.getDate()).toString();
        try (Jedis jedis = jedisCache.getConnection()) {
            jedis.hmset(key, appointmentDate.getAsMap());
            jedis.expire(key, EXPIRATION_TIME_SEC);
        } catch (JedisException e) {
            log.error(e);
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
            boolean doScan = false;
            log.info("UpdateTask checks data cached locally");
            for (AppointmentDate.Type type : AppointmentDate.Type.values()) {
                TreeMap<Long, AppointmentDate> map = availableAppointmentDates.get(type);
                if (map.size() < daysToScan) {
                    log.info(type.name() + " appointment info is not full: " + map.size());
                    doScan = true;
                    continue;
                }

                Entry<Long, AppointmentDate> entry = map.firstEntry();
                long updatedAt = entry.getValue().getUpdatedAt();
                long dataKeepTime = System.currentTimeMillis() - updatedAt;
                long dataKeepTimeMin = dataKeepTime / (1000 * 60);
                log.info("Data updated " + dataKeepTimeMin + " min ago");
                if (dataKeepTime > UPDATE_PERIOD)
                    doScan = true;
            }
            if (doScan)
                updateAppointmentDates();
            removeOldDatesFromCache(dateFormat);
        }

        private void updateAppointmentDates() {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, 120);
            long startScan = System.currentTimeMillis();
            log.info("Start scan");
            for (int i = 0; i < daysToScan; i++) {
                String date = dateFormat.format(calendar.getTime());
                for (AppointmentDate.Type type : AppointmentDate.Type.values()) {
                    try {
                        long updatedAt = System.currentTimeMillis();
                        AppointmentDate appointmentDate = ServerAPI.getDateInfo(date, type);
                        appointmentDate.setUpdatedAt(updatedAt);
                        log.info("Updated: " + appointmentDate.toString());
                        updateLocalCache(appointmentDate, dateFormat);
                        cacheRemote(appointmentDate);
                    } catch (IOException e) {
                        log.error("Exception occurred while sending request to urzad", e);
                    }
                    try {
                        Thread.sleep(REQUEST_DELAY);
                    } catch (InterruptedException e) {
                        log.error("Exception occurred while sleep: " + e.getMessage());
                    }
                }
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            long scanTime = System.currentTimeMillis() - startScan;
            log.info("Scan complete. Time taken: " + scanTime);
        }

        /**
         * Remove expired information from cache. This is the only thread which performs write operations with cache. Don't need to do read lock here
         */
        private void removeOldDatesFromCache(DateFormat dateFormat) {
            long currentTime = System.currentTimeMillis();
            String currentDate = Utils.millsToDate(currentTime, dateFormat);

            for (AppointmentDate.Type type : availableAppointmentDates.keySet()) {
                Long firstKeyTime = availableAppointmentDates.get(type).firstKey();
                String firstKeyDate = Utils.millsToDate(firstKeyTime, dateFormat);
                if (!currentDate.equals(firstKeyDate) && firstKeyTime < currentTime) {
                    writeLock.lock();
                    try {
                        availableAppointmentDates.get(type).remove(firstKeyTime);
                    } finally {
                        writeLock.unlock();
                    }
                }
            }
        }
    }
}
