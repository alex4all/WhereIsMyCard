package org.bot.commands.impl;

import org.bot.appointment.AppointmentDate;
import org.bot.appointment.AppointmentsManager;
import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.bot.commands.Context;
import org.bot.keyboards.adapter.CalendarKeyboardAdapter;
import org.bot.keyboards.adapter.MonthKeyboardAdapter;
import org.bot.utils.EditText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Update;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

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

    private String dateInfoToString(String date, Map<AppointmentDate.Type, AppointmentDate> dateInfo) {
        StringBuilder result = new StringBuilder(EditText.bold(date)).append(System.lineSeparator());

        for (Map.Entry<AppointmentDate.Type, AppointmentDate> entry : dateInfo.entrySet()) {
            AppointmentDate.Type type = entry.getKey();
            AppointmentDate appointment = entry.getValue();
            result.append(EditText.bold(type.name())).append(": ");
            if (appointment == null) {
                result.append(getResource("command.dateInfo.noData")).append(System.lineSeparator());
                continue;
            }
            if (!appointment.isAvailable())
                result.append(getResource("command.dateInfo.noAvailableTime")).append(" ");
            else {
                result.append(appointment.getAvailableTime()).append(" ");
            }
            long minutesAgo = appointment.getTimeAfterUpdate();
            String updatedMinutesAgo = EditText.timeAfterUpdate(minutesAgo, this);
            result.append(EditText.italic(updatedMinutesAgo)).append(System.lineSeparator());
        }
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
            Map<AppointmentDate.Type, AppointmentDate> dateInfo = DATES_MANAGER.getDateInfo(date);
            String text = dateInfoToString(dateFormat.format(date), dateInfo);
            context.sendOrEditLast(text);
            context.ignoreCallback(query);
        }

        @Override
        public void onBackClick(Context context, CallbackQuery query) {
            monthAdapter.display(context);
            context.ignoreCallback(query);
        }
    }

    @Override
    public String getName() {
        return "date_info";
    }
}
