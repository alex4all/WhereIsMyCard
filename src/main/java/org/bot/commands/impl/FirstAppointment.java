package org.bot.commands.impl;

import org.bot.appointment.AppointmentDate;
import org.bot.appointment.AppointmentsManager;
import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.bot.keyboards.Button;
import org.bot.keyboards.HorizontalKeyboard;
import org.bot.utils.MessageUtils;
import org.telegram.telegrambots.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@BotCommand(name = "first_appointment")
public class FirstAppointment extends Command {
    private static final AppointmentsManager DATES_MANAGER = AppointmentsManager.getInstance();
    private static final String SELECT_MESSAGE = "Select appointment type";

    @Override
    public void process(CommandResultHandler handler, Update update) {
        List<Button> buttons = new ArrayList<>(AppointmentDate.Type.values().length);
        for (AppointmentDate.Type type : AppointmentDate.Type.values()) {
            buttons.add(new Button(type.name(), type.name()));
        }
        HorizontalKeyboard keyboard = new HorizontalKeyboard();
        keyboard.setButtons(buttons);
        System.out.println("Send new keyboard");
        MessageUtils.sendMessage(handler, update, SELECT_MESSAGE, keyboard.create());
    }

    public void processCallbackQuery(CommandResultHandler handler, Update update) {
        String callbackQuery = update.getCallbackQuery().getData();
        AppointmentDate.Type type = AppointmentDate.Type.valueOf(callbackQuery);
        List<AppointmentDate> datesInfo = DATES_MANAGER.getFirstAvailableDates(type, 1);
        String firstAppointment = datesInfoToString(datesInfo);
        if (lastBotMessage != null) {
            if (lastBotMessage.getText().equals(firstAppointment))
                return;
            System.out.println("edit message: " + lastBotMessage);
            MessageUtils.edit(handler, lastBotMessage, firstAppointment);
        } else {
            lastBotMessage = MessageUtils.sendMessage(handler, update, firstAppointment);
            System.out.println("lastBotMessage: " + lastBotMessage);
        }
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
