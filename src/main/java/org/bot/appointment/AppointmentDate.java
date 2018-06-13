package org.bot.appointment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentDate {
    private static final Logger log = LogManager.getLogger(AppointmentDate.class);
    private static final String NEGATIVE_RESULT = "brak";

    public enum Type {ODBIOR, ZLOZENIE}

    private Type type;
    private String date;
    private List<String> availableTime;
    private long updatedAt;
    boolean available = true;

    public AppointmentDate(Type type, String date, String availableTime) {
        this.type = type;
        this.date = date;
        this.updatedAt = System.currentTimeMillis();
        if (availableTime.startsWith(NEGATIVE_RESULT)) {
            available = false;
        } else {
            availableTime = availableTime.replaceAll("  ", " ");
            String[] availableTimeArray = availableTime.split(" ");
            this.availableTime = Arrays.asList(availableTimeArray);
        }
    }

    public AppointmentDate(Map<String, String> map) {
        log.info(map);
        type = Type.valueOf(map.get("type"));
        date = map.get("date");
        updatedAt = Long.parseLong(map.get("updatedAt"));
        available = Boolean.parseBoolean(map.get("available"));
        if (available) {
            String time = map.get("availableTime").replaceAll("  ", " "); // TODO tmp fix for backward compatibility
            String[] availableTimeArray = time.split(" ");
            availableTime = Arrays.asList(availableTimeArray);
        }
    }

    public void update(AppointmentDate newDate) {
        this.availableTime = newDate.availableTime;
        this.updatedAt = newDate.updatedAt;
        this.available = newDate.available;
    }

    public Map<String, String> getAsMap() {
        Map<String, String> map = new HashMap<>();
        map.put("type", type.name());
        map.put("date", date);
        map.put("updatedAt", String.valueOf(updatedAt));
        map.put("available", String.valueOf(available));
        if (available) {
            StringBuilder availableTimeBuilder = new StringBuilder();
            for (String time : availableTime)
                availableTimeBuilder.append(time).append(" ");
            map.put("availableTime", availableTimeBuilder.substring(0, availableTimeBuilder.length() - 1));
        }
        return map;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getTimeAfterUpdate() {
        long timeAfterUpdate = System.currentTimeMillis() - updatedAt;
        return timeAfterUpdate / (1000 * 60);
    }

    public List<String> getAvailableTime() {
        return availableTime;
    }

    public Type getType() {
        return type;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(date);
        return "Type: " + type + "; Date: " + date + ": " + availableTime + "; " + updatedAt;
    }
}
