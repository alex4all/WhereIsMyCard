package org.bot.utils;

import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MonthKyeboardBuilder {
    public static final String[] monthNames = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    private Date begin;
    private Date end;

    public MonthKyeboardBuilder begin(Date begin) {
        this.begin = begin;
        return this;
    }

    public MonthKyeboardBuilder end(Date end) {
        this.end = end;
        return this;
    }

    public ReplyKeyboardMarkup create()
    {
        if(begin.getTime() > end.getTime())
            throw new RuntimeException("Begin time can't be more than end time");
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>(8);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(begin);
        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTime(end);
        do {
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            KeyboardRow row = new KeyboardRow();
            String msg = monthNames[month] + " " + year;
            System.out.println("Added: " + msg);
            row.add(monthNames[month] + " " + year);
            rows.add(row);
            calendar.set(Calendar.MONTH, month + 1);
        } while(calendar.get(Calendar.MONTH) != calendarEnd.get(Calendar.MONTH));
        markup.setKeyboard(rows);
        return markup;
    }
}
