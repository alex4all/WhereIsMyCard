package org.bot.keyboards.adapter;

import org.bot.commands.Command;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

public abstract class ExcludingCalendarAdapter extends CalendarKeyboardAdapter {

    private Set<Integer> availableDays;

    public ExcludingCalendarAdapter(Date begin, Date end, Set<Integer> availableDays) {
        super(begin, end);
        this.availableDays = availableDays;
    }

    /**
     * Allows to override default behavior and implement different data display strategies
     *
     * @param calendar
     * @return
     */
    protected String getDayText(Calendar calendar) {
        if (availableDays.contains(calendar.get(Calendar.DAY_OF_YEAR)))
            return String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        return "x";
    }

    /**
     * Allows to override default behavior and implement different data display strategies
     *
     * @param calendar
     * @return
     */
    protected String getDayCallback(Calendar calendar) {
        if (availableDays.contains(calendar.get(Calendar.DAY_OF_YEAR)))
            return Event.CLICK_DAY + callbackFormat.format(calendar.getTime());
        return Command.IGNORE_QUERY;
    }
}
