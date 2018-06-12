package org.bot.commands.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bot.appointment.AppointmentDate;
import org.bot.appointment.AppointmentsManager;
import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.bot.keyboards.Button;
import org.bot.keyboards.HorizontalKeyboard;
import org.telegram.telegrambots.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@BotCommand(name = "first_appointment")
public class FirstAppointment extends Command {
    private static final Logger log = LogManager.getLogger(FirstAppointment.class);
    private static final AppointmentsManager DATES_MANAGER = AppointmentsManager.getInstance();
    private static final String SELECT_MESSAGE = "Select appointment type";

    public FirstAppointment(CommandResultHandler handler, Update update) {
        super(handler, update);
    }

    @Override
    public void process(Update update) {
        List<Button> buttons = new ArrayList<>(AppointmentDate.Type.values().length);
        for (AppointmentDate.Type type : AppointmentDate.Type.values()) {
            buttons.add(new Button(type.name(), type.name()));
        }
        HorizontalKeyboard keyboard = new HorizontalKeyboard();
        keyboard.setButtons(buttons);
        log.info("Send new keyboard");
        showKeyboard(SELECT_MESSAGE, keyboard.create());
    }

    @Override
    public void processCallbackQuery(Update update) {
        String callbackQuery = update.getCallbackQuery().getData();
        AppointmentDate.Type type = AppointmentDate.Type.valueOf(callbackQuery);
        List<AppointmentDate> datesInfo = DATES_MANAGER.getFirstAvailableDates(type, 1);
        String firstAppointment = datesInfoToString(datesInfo);
        sendOrEditLast(firstAppointment);
    }

    private String datesInfoToString(List<AppointmentDate> datesInfo) {
        if (datesInfo.size() == 0) {
            return "No data found";
        }
        StringBuilder result = new StringBuilder();
        for (AppointmentDate dayInfo : datesInfo)
            result.append(dayInfo.toMessageWithBoth()).append(System.lineSeparator());
        return result.toString();
    }
}
