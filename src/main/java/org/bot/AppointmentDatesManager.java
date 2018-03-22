package org.bot;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

public class AppointmentDatesManager {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final long REQUEST_DELAY = 2000;
    private static final int MAX_AVAILABLE_DATES = 10;

    private Map<String, AppointmentDate> appointmentMap;
    private TreeMap<Long, AppointmentDate> sortedAppointmentMap;
    private ReentrantLock mapsLock;

    public AppointmentDatesManager(int daysToScan, long updatePeriod) {
        appointmentMap = new HashMap<>();
        sortedAppointmentMap = new TreeMap<>();
        mapsLock = new ReentrantLock();
        Timer timer = new Timer(true);
        TimerTask timerTask = new UpdateTask(daysToScan);
        timer.schedule(timerTask, 0, updatePeriod);

    }

    public AppointmentDate getDateInfo(String date) {
        mapsLock.lock();
        try {
            return appointmentMap.get(date);
        } finally {
            mapsLock.unlock();
        }
    }

    public List<AppointmentDate> getFirstAvailableDates(int count) {
        List<AppointmentDate> dates = new ArrayList<>();
        if (count > MAX_AVAILABLE_DATES)
            count = MAX_AVAILABLE_DATES;
        if (count < 1)
            count = 1;
        mapsLock.lock();
        try {
            for (Entry<Long, AppointmentDate> entry : sortedAppointmentMap.entrySet()) {
                if (entry.getValue().isAvailable())
                    dates.add(entry.getValue());
                if (dates.size() >= count)
                    return dates;
            }
            return dates;
        } finally {
            mapsLock.unlock();
        }
    }

    /**
     * Remove old dates from cache
     */
    private void cleanOldDates() {
        long currentTime = System.currentTimeMillis();
        long firstKeyTime = sortedAppointmentMap.firstKey();

        String currentTimeStr = DATE_FORMAT.format(new Date(currentTime));
        String firstKeyTimeStr = DATE_FORMAT.format(new Date(firstKeyTime));

        if (!currentTimeStr.equals(firstKeyTimeStr) && firstKeyTime < currentTime) {
            AppointmentDate dayInfo = sortedAppointmentMap.remove(firstKeyTime);
            System.out.println("Remove data from map: " + dayInfo.toString());
            appointmentMap.remove(dayInfo.getDate());
        }
    }

    private void update(long updatedAt, String date, long dateAsMills, String availableTime) {
        AppointmentDate dayInfo = new AppointmentDate(updatedAt, date, availableTime);
        System.out.println("Data updated: " + dayInfo.toString());
        mapsLock.lock();
        try {
            sortedAppointmentMap.put(dateAsMills, dayInfo);
            appointmentMap.put(dayInfo.getDate(), dayInfo);

        } finally {
            mapsLock.unlock();
        }
    }

    private class UpdateTask extends TimerTask {
        private final SimpleDateFormat dateFormat;
        private final int daysToScan;

        UpdateTask(int daysToScan) {
            this.daysToScan = daysToScan;
            this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        }

        @Override
        public void run() {

            Calendar calendar = Calendar.getInstance();
            for (int i = 0; i < daysToScan; i++) {
                try {
                    long updatedAt = System.currentTimeMillis();
                    String date = dateFormat.format(calendar.getTime());
                    long dateAsMills = dateFormat.parse(date).getTime();
                    String availableTime = ServerAPI.getAvailableTime(date);
                    update(updatedAt, date, dateAsMills, availableTime);
                } catch (Exception e) {
                    System.out.println("Exception occured while sending request to urzand : " + e.getMessage());
                    e.printStackTrace();
                }

                calendar.add(Calendar.DAY_OF_YEAR, 1);
                try {
                    Thread.sleep(REQUEST_DELAY);
                } catch (InterruptedException e) {
                    System.out.println("Exception occured while sleep: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            cleanOldDates();
        }
    }
}
