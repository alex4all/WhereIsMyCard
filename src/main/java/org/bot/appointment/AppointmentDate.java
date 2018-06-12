package org.bot.appointment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class AppointmentDate {
    private static final Logger log = LogManager.getLogger(AppointmentDate.class);
    private static final String NEGATIVE_RESULT = "brak";
    public enum Type {ODBIOR, ZLOZENIE}


    private Type type;
    private String date;
    private String availableTime;
    private long updatedAt;
    boolean available = true;

    public AppointmentDate(Type type, String date, String availableTime) {
        this.type = type;
        this.date = date;
        this.updatedAt = System.currentTimeMillis();
        if (availableTime.startsWith(NEGATIVE_RESULT)) {
            this.availableTime = "No available time";
            available = false;
        } else
            this.availableTime = availableTime;
    }

    public AppointmentDate(Map<String, String> map)
    {
        log.info(map);
        type = Type.valueOf(map.get("type"));
        date = map.get("date");
        availableTime = map.get("availableTime");
        updatedAt = Long.parseLong(map.get("updatedAt"));
        available = Boolean.parseBoolean(map.get("available"));
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
        map.put("availableTime", availableTime);
        map.put("updatedAt", String.valueOf(updatedAt));
        map.put("available", String.valueOf(available));
        return map;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
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

    public String getTimeAfterUpdate() {
        long timeAfterUpdate = System.currentTimeMillis() - updatedAt;
        long minutesAgo = timeAfterUpdate / (1000 * 60);
        if (minutesAgo < 1)
            return "Just updated";
        return "Updated " + minutesAgo + " min ago";
    }

    public String getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(String availableTime) {
        this.availableTime = availableTime;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(date);
        return "Type: " + type + "; Date: " + date + ": " + availableTime + "; " + getTimeAfterUpdate();
    }

    public String toMessageWithDate() {
        StringBuilder builder = new StringBuilder();
        builder.append("<b>").append(date).append("</b>").append(": ");
        builder.append(availableTime).append(" <i>").append(getTimeAfterUpdate()).append("</i>");
        return builder.toString();
    }

    public String toMessageWithType() {
        StringBuilder builder = new StringBuilder();
        builder.append("<b>").append(type).append("</b>").append(": ");
        builder.append(availableTime).append(" <i>").append(getTimeAfterUpdate()).append("</i>");
        return builder.toString();
    }

    public String toMessageWithBoth() {
        StringBuilder builder = new StringBuilder();
        builder.append("<b>").append(type).append(" ").append(date).append("</b>").append(":").append(System.lineSeparator());
        builder.append(availableTime).append(" <i>").append(getTimeAfterUpdate()).append("</i>");
        return builder.toString();
    }
}
