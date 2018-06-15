package org.bot.keyboards.adapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bot.appointment.AppointmentDate;
import org.bot.commands.Command;
import org.bot.commands.Context;
import org.bot.keyboards.Button;
import org.bot.keyboards.GridKeyboard;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AppointmentTimeAdapter implements KeyboardAdapter {
    private static final Logger log = LogManager.getLogger(AppointmentTimeAdapter.class);
    private static final String TIME_CLICK = "TIME_CLICK";
    private AppointmentDate appointment;
    private String callbackCommand;
    private int rowSize = 4;

    public AppointmentTimeAdapter(AppointmentDate appointment, String callbackCommand) {
        this.appointment = appointment;
        this.callbackCommand = callbackCommand;
    }

    public InlineKeyboardMarkup build() {
        GridKeyboard keyboard = new GridKeyboard();
        String date = appointment.getDate();
        String type = appointment.getType().name();
        Button header = new Button(type + ": " + date, Command.IGNORE_QUERY);
        keyboard.addRow(Arrays.asList(header));

        List<String> availableTime = appointment.getAvailableTime();
        int timeIndex = 0;
        while (timeIndex < availableTime.size()) {
            List<Button> row = new ArrayList<>(rowSize);
            for (int i = 0; i < rowSize; i++) {
                String text;
                String callback;
                if (timeIndex < availableTime.size()) {
                    text = availableTime.get(timeIndex);
                    callback = "/" + callbackCommand + TIME_CLICK + text;
                } else {
                    text = " ";
                    callback = Command.IGNORE_QUERY;
                }
                row.add(new Button(text, callback));
            }
            keyboard.addRow(row);
        }
        return keyboard.build();
    }

    public static boolean isCallbackHandler(String queryText) {
        if (queryText.startsWith(TIME_CLICK))
            return true;
        return false;
    }

    @Override
    public boolean processCallback(Context context, Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String queryText = callbackQuery.getData();
        if (queryText.startsWith(TIME_CLICK)) {
            String time = queryText.substring(TIME_CLICK.length());
        }
        return false;
    }

    public abstract void onTimeClick(AppointmentDate.Type type, String date, String time, CallbackQuery callbackQuery);
}
