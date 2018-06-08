package org.bot.commands.impl;

import org.bot.appointment.AppointmentDate;
import org.bot.appointment.AppointmentsManager;
import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.bot.keyboards.adapter.CalendarKeyboardAdapter;
import org.bot.keyboards.adapter.MonthKeyboardAdapter;
import org.bot.utils.MessageUtils;
import org.telegram.telegrambots.api.objects.Update;

import java.text.ParseException;
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
    public DateInfo() {
        Calendar calendar = Calendar.getInstance();
        Date begin = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, AppointmentsManager.DAYS_TO_SCAN);
        Date end = calendar.getTime();
        monthAdapter = new DateInfoMonthAdapter(begin, end);
        calendarAdapter = new DateInfoCalendarAdapter(begin, end);

    }

    @Override
    public void process(CommandResultHandler handler, Update update) {
        monthAdapter.display(handler, update.getMessage().getChatId());
    }

    @Override
    public void processCallbackQuery(CommandResultHandler handler, Update update) {
        if (monthAdapter.processCallback(handler, update))
            return;
        calendarAdapter.processCallback(handler, update);
    }

    private String dateInfoToString(String date, List<AppointmentDate> dateInfo) {
        // verify thar result is valid. no null elements
        List<AppointmentDate> verifiedDates = new ArrayList<>();
        for (AppointmentDate appDate : dateInfo) {
            if (appDate != null)
                verifiedDates.add(appDate);
        }

        if (verifiedDates.size() == 0) {
            StringBuilder result = new StringBuilder();
            result.append("<b>").append(date).append(":</b>").append("No data found");
            return result.toString();
        }

        StringBuilder result = new StringBuilder();
        result.append("<b>").append(date).append(":</b>").append(System.lineSeparator());

        for (AppointmentDate appDate : dateInfo)
            result.append(appDate.toMessageWithType()).append(System.lineSeparator());
        return result.toString();
    }

    private class DateInfoMonthAdapter extends MonthKeyboardAdapter {

        public DateInfoMonthAdapter(Date begin, Date end) {
            super(begin, end);
        }

        @Override
        public void onMonthClick(Date date, CommandResultHandler handler, Update update) {
            // share message with keyboard to edit it
            calendarAdapter.setKeyboardMessage(getKeyboardMessage());
            calendarAdapter.display(date, handler, update.getCallbackQuery().getMessage().getChatId());
        }
    }

    private class DateInfoCalendarAdapter extends CalendarKeyboardAdapter {
        public DateInfoCalendarAdapter(Date begin, Date end) {
            super(begin, end);
        }

        @Override
        public void onDayClick(Date date, CommandResultHandler handler, Update update) {
            try {
                List<AppointmentDate> result = DATES_MANAGER.getDateInfo(date);
                Long chatId = update.getCallbackQuery().getMessage().getChatId();
                String text = dateInfoToString(dateFormat.format(date), result);
                lastBotMessage = MessageUtils.sendOrEdit(lastBotMessage, chatId, text, handler);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onBackClick(CommandResultHandler handler, Update update) {
            // share message with keyboard to edit it
            monthAdapter.setKeyboardMessage(getKeyboardMessage());
            monthAdapter.display(handler, update.getCallbackQuery().getMessage().getChatId());
        }
    }
}
