package org.bot;

public class AppointmentDate {
	private long updatedAt;
	private String date;
	private String availableTime;
	boolean available = true;

	public AppointmentDate(long updatedAt, String date, String availableTime) {
		this.updatedAt = updatedAt;
		this.date = date;
		this.availableTime = availableTime;
		if (availableTime.startsWith("brak"))
			available = false;
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

	public String getTime() {
		return availableTime;
	}

	public void setTime(String availableTime) {
		this.availableTime = availableTime;
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
			return "less than 1 min ago";
		return minutesAgo + "min ago";
	}

	public String toString() {
		return date + ": " + availableTime + "; Updated " + getTimeAfterUpdate();
	}
}
