package org.bot.keyboards.adapter;

import org.bot.appointment.AppointmentsManager;
import org.bot.commands.Command;
import org.bot.commands.Context;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

public abstract class ExcludingCalendarAdapter extends CalendarKeyboardAdapter {

    private Set<Integer> availableDays;

    public ExcludingCalendarAdapter(Date begin, Date end) {
        super(begin, end);
    }

    /**
     * Allows to override default behavior and implement different data display strategies
     *
     * @param calendar
     * @return
     */
    protected String getDayText(Calendar calendar) {
        if (availableDays.contains(calendar.get(Calendar.DAY_OF_MONTH)))
            return String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        return "-";
    }

    public void display(Date date, Context context) {
        availableDays = AppointmentsManager.getInstance().getAvailableDays(date);
        super.display(date, context);
    }

    /**
     * Allows to override default behavior and implement different data display strategies
     *
     * @param calendar
     * @return
     */
    protected String getDayCallback(Calendar calendar) {
        if (availableDays.contains(calendar.get(Calendar.DAY_OF_MONTH)))
            return Event.CLICK_DAY + callbackFormat.format(calendar.getTime());
        return Command.IGNORE_QUERY;
    }
}
