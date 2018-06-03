package org.bot.commands.impl;

import org.bot.appointment.AppointmentDate;
import org.bot.appointment.AppointmentDatesManager;
import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.bot.utils.CalendarKeyboard;
import org.bot.utils.MonthsKeyboard;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@BotCommand(name = "date_info")
public class DateInfo extends Command {
    private static final AppointmentDatesManager DATES_MANAGER = AppointmentDatesManager.getInstance();
    private Message lastBotBessage;

    @Override
    public void process(CommandResultHandler handler, Update update) {
        showMonthsKeyboard(handler, update, false);
    }

    private void showMonthsKeyboard(CommandResultHandler handler, Update update, boolean edit) {
        Calendar calendar = Calendar.getInstance();
        Date begin = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, AppointmentDatesManager.DAYS_TO_SCAN);
        Date end = calendar.getTime();
        MonthsKeyboard builder = new MonthsKeyboard().begin(begin).end(end);

        if (edit) {
            EditMessageText message = new EditMessageText()
                    .enableHtml(true)
                    .setChatId(getChatId(update))
                    .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                    .setText("Select date")
                    .setReplyMarkup(builder.create());
            handler.execute(message);
        } else {
            SendMessage message = new SendMessage()
                    .enableHtml(true)
                    .setChatId(getChatId(update))

                    .setText("Select date")
                    .setReplyMarkup(builder.create());
            handler.execute(message);
        }
    }

    @Override
    public void processCallbackQuery(CommandResultHandler handler, Update update) {
        String data = update.getCallbackQuery().getData();
        if (data.startsWith(MonthsKeyboard.DEF_CALLBACK_PREFIX)) {
            String date = data.substring(MonthsKeyboard.DEF_CALLBACK_PREFIX.length());
            System.out.println("Callback date: " + date);

            CalendarKeyboard builder = new CalendarKeyboard();
            try {
                Date month = new SimpleDateFormat(MonthsKeyboard.DEF_CALLBACK_DATE_PATTERN).parse(date);
                System.out.println("Parsed date: " + month.toString());
                builder.date(month);
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

        } else if (data.startsWith(CalendarKeyboard.DEF_CALLBACK_PREFIX)) {
            System.out.println("get data for provided date: " + data);

            String date = data.substring(CalendarKeyboard.DEF_CALLBACK_PREFIX.length());
            System.out.println("Callback date without prefix: " + date);
            try {
                Date dayOfMonth = new SimpleDateFormat(CalendarKeyboard.DEF_CALLBACK_DATE_PATTERN).parse(date);
                System.out.println("Parsed date: " + dayOfMonth.toString());
                List<AppointmentDate> result = DATES_MANAGER.getDateInfo(dayOfMonth);

                if (lastBotBessage == null) {
                    SendMessage message = new SendMessage()
                            .enableHtml(true)
                            .setChatId(update.getCallbackQuery().getMessage().getChatId())
                            .setText(dateInfoToString(result));
                    lastBotBessage = handler.execute(message);
                } else {
                    EditMessageText message = new EditMessageText()
                            .enableHtml(true)
                            .setChatId(update.getCallbackQuery().getMessage().getChatId())
                            .setMessageId(lastBotBessage.getMessageId())
                            .setText(dateInfoToString(result));
                    handler.execute(message);
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        } else if (data.equals(CalendarKeyboard.DEF_CALLBACK_BACK)) {
            showMonthsKeyboard(handler, update, true);
        }
    }

    private String dateInfoToString(List<AppointmentDate> dateInfo) {
        // verify thar result is valid. no null elements
        List<AppointmentDate> verifiedDates = new ArrayList<>();
        for (AppointmentDate appDate : dateInfo) {
            if (appDate != null)
                verifiedDates.add(appDate);
        }

        if (verifiedDates.size() == 0) {
            return "No data found for provided date";
        }

        StringBuilder result = new StringBuilder();
        result.append("<b>").append(verifiedDates.get(0).getDate()).append(":</b>").append(System.lineSeparator());

        for (AppointmentDate appDate : dateInfo)
            result.append(appDate.toMessageWithType()).append(System.lineSeparator());
        return result.toString();
    }
}
