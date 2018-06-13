package org.bot.keyboards.adapter;

import java.util.Date;
import java.util.Set;

public abstract class ExcludingMonthAdapter extends MonthKeyboardAdapter {
    private Set<Integer> availableMonths;

    public ExcludingMonthAdapter(Date begin, Date end, Set<Integer> availableMonths) {
        super(begin, end);
        this.availableMonths = availableMonths;
    }
}
