package org.bot.appointment;

public class AppointmentDate {
    public enum Type {ODBIOR, ZLOZENIE}

    private Type type;
    private String date;
    private String availableTime;
    private long updatedAt;
    boolean available = true;

    public AppointmentDate(Type type, String date, String availableTime) {
        this.type = type;
        this.date = date;
        if (availableTime.startsWith("brak")) {
            this.availableTime = "No available time";
            available = false;
        } else
            this.availableTime = availableTime;
    }

    public void update(AppointmentDate newDate) {
        this.availableTime = newDate.availableTime;
        this.updatedAt = newDate.updatedAt;
        this.available = newDate.available;
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
            return "Updated < 1 min ago";
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
}
