package org.bot.commands.impl;

import org.bot.appointment.AppointmentDate;
import org.bot.appointment.AppointmentsManager;
import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.bot.commands.Context;
import org.bot.keyboards.adapter.CalendarKeyboardAdapter;
import org.bot.keyboards.adapter.MonthKeyboardAdapter;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Update;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@BotCommand(name = "date_info")
public class DateInfo extends Command {
    private static final AppointmentsManager DATES_MANAGER = AppointmentsManager.getInstance();
    private MonthKeyboardAdapter monthAdapter;
    private CalendarKeyboardAdapter calendarAdapter;
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);

    public DateInfo(CommandResultHandler handler, Update update) {
        super(handler, update);
        Calendar calendar = Calendar.getInstance();
        Date begin = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, AppointmentsManager.DAYS_TO_SCAN);
        Date end = calendar.getTime();
        monthAdapter = new DateInfoMonthAdapter(begin, end);
        calendarAdapter = new DateInfoCalendarAdapter(begin, end);
    }

    @Override
    public void process(Update update) {
        monthAdapter.display(this);
    }

    @Override
    public void processCallbackQuery(Update update) {
        if (monthAdapter.processCallback(this, update))
            return;
        calendarAdapter.processCallback(this, update);
    }

    private String dateInfoToString(String date, List<AppointmentDate> dateInfo) {
        // verify thar result is valid. no null elements
        List<AppointmentDate> verifiedDates = new ArrayList<>();
        for (AppointmentDate appDate : dateInfo) {
            if (appDate != null)
                verifiedDates.add(appDate);
        }

        if (verifiedDates.size() == 0) {
            return new StringBuilder()
                    .append("<b>").append(date).append(":</b>")
                    .append("No data found").toString();
        }

        StringBuilder result = new StringBuilder();
        result.append("<b>").append(date).append(":</b>").append(System.lineSeparator());

        for (AppointmentDate appDate : dateInfo)
            if (appDate != null)
                result.append(appDate.toMessageWithType()).append(System.lineSeparator());
        return result.toString();
    }

    private class DateInfoMonthAdapter extends MonthKeyboardAdapter {

        public DateInfoMonthAdapter(Date begin, Date end) {
            super(begin, end);
        }

        @Override
        public void onMonthClick(Date date, Context context, Update update) {
            calendarAdapter.display(date, context);
        }
    }

    private class DateInfoCalendarAdapter extends CalendarKeyboardAdapter {
        public DateInfoCalendarAdapter(Date begin, Date end) {
            super(begin, end);
        }

        @Override
        public void onDayClick(Date date, Context context, CallbackQuery query) {
            List<AppointmentDate> result = DATES_MANAGER.getDateInfo(date);
            String text = dateInfoToString(dateFormat.format(date), result);
            context.sendOrEditLast(text);
            context.ignoreCallback(query);
        }

        @Override
        public void onBackClick(Context context, CallbackQuery query) {
            monthAdapter.display(context);
            context.ignoreCallback(query);
        }
    }
}
