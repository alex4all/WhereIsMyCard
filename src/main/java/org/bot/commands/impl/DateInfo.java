package org.bot.commands.impl;

import org.bot.appointment.AppointmentDate;
import org.bot.appointment.AppointmentsManager;
import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.bot.keyboards.CalendarKeyboard;
import org.bot.keyboards.MonthsKeyboard;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@BotCommand(name = "date_info")
public class DateInfo extends Command {
    private static final AppointmentsManager DATES_MANAGER = AppointmentsManager.getInstance();
    private Message lastBotMessage;

    @Override
    public void process(CommandResultHandler handler, Update update) {
        showMonthsKeyboard(handler, update, false);
    }

    @Override
    public void processCallbackQuery(CommandResultHandler handler, Update update) {
        String callbackQuery = update.getCallbackQuery().getData();
        System.out.println("processCallbackQuery callbackQuery: " + callbackQuery);
        if (callbackQuery.startsWith(MonthsKeyboard.MONTH_CLICK_PREFIX)) {
            String date = callbackQuery.substring(MonthsKeyboard.MONTH_CLICK_PREFIX.length());
            showCalendarKeyboard(date, handler, update);
        } else if (callbackQuery.startsWith(CalendarKeyboard.PREVIOUS_CLICK)) {
            String date = callbackQuery.substring(CalendarKeyboard.PREVIOUS_CLICK.length());
            showCalendarKeyboard(date, handler, update);
        } else if (callbackQuery.startsWith(CalendarKeyboard.NEXT_CLICK_PREFIX)) {
            String date = callbackQuery.substring(CalendarKeyboard.NEXT_CLICK_PREFIX.length());
            showCalendarKeyboard(date, handler, update);
        } else if (callbackQuery.startsWith(CalendarKeyboard.DAY_CLICK_PREFIX)) {
            showDayInfo(handler, update);
        } else if (callbackQuery.equals(CalendarKeyboard.BACK_CLICK_PREFIX)) {
            showMonthsKeyboard(handler, update, true);
        } else if (callbackQuery.startsWith(CalendarKeyboard.WARNING_PREFIX)) {
            printWarning(handler, update);
        }
    }

    private void printWarning(CommandResultHandler handler, Update update) {
        String data = update.getCallbackQuery().getData();
        String warning = data.substring(CalendarKeyboard.WARNING_PREFIX.length());
        sendOrEdit(handler, update, warning);
    }

    private void showMonthsKeyboard(CommandResultHandler handler, Update update, boolean edit) {
        Calendar calendar = Calendar.getInstance();
        Date begin = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, AppointmentsManager.DAYS_TO_SCAN);
        Date end = calendar.getTime();
        System.out.println("showMonthsKeyboard in interval: [" + begin.toString() + "; " + end.toString() + "]");
        MonthsKeyboard builder = new MonthsKeyboard().begin(begin).end(end);
        InlineKeyboardMarkup keyboard = builder.create();
        if (edit) {
            System.out.println("Edit existing keyboard");
            EditMessageText message = new EditMessageText()
                    .enableHtml(true)
                    .setChatId(getChatId(update))
                    .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                    .setText("Select month")
                    .setReplyMarkup(keyboard);
            handler.execute(message);
        } else {
            System.out.println("Send new keyboard");
            SendMessage message = new SendMessage()
                    .enableHtml(true)
                    .setChatId(getChatId(update))
                    .setText("Select month")
                    .setReplyMarkup(keyboard);
            handler.execute(message);
        }
    }

    private void showCalendarKeyboard(String date, CommandResultHandler handler, Update update) {
        Calendar calendar = Calendar.getInstance();
        Date begin = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, AppointmentsManager.DAYS_TO_SCAN);
        Date end = calendar.getTime();
        System.out.println("showCalendarKeyboard month: " + date);
        CalendarKeyboard builder = new CalendarKeyboard().begin(begin).end(end);
        try {
            Date month = new SimpleDateFormat(MonthsKeyboard.DEF_CALLBACK_DATE_PATTERN).parse(date);
            System.out.println("Parsed month: " + month.toString());
            builder.month(month);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        EditMessageText message = new EditMessageText()
                .setChatId(update.getCallbackQuery().getMessage().getChatId())
                .enableHtml(true)
                .setText("Select day of month")
                .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                .setReplyMarkup(builder.create());
        handler.execute(message);
    }

    private void showDayInfo(CommandResultHandler handler, Update update) {
        String data = update.getCallbackQuery().getData();
        System.out.println("get data for provided month: " + data);
        String date = data.substring(CalendarKeyboard.DAY_CLICK_PREFIX.length());
        System.out.println("Callback month without prefix: " + date);
        try {
            Date dayOfMonth = new SimpleDateFormat(CalendarKeyboard.DEF_CALLBACK_DATE_PATTERN).parse(date);
            System.out.println("Parsed month: " + dayOfMonth.toString());
            List<AppointmentDate> result = DATES_MANAGER.getDateInfo(dayOfMonth);

            if (lastBotMessage == null) {
                SendMessage message = new SendMessage()
                        .enableHtml(true)
                        .setChatId(update.getCallbackQuery().getMessage().getChatId())
                        .setText(dateInfoToString(date, result));
                lastBotMessage = handler.execute(message);
            } else {
                EditMessageText message = new EditMessageText()
                        .enableHtml(true)
                        .setChatId(update.getCallbackQuery().getMessage().getChatId())
                        .setMessageId(lastBotMessage.getMessageId())
                        .setText(dateInfoToString(date, result));
                handler.execute(message);
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
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
}
