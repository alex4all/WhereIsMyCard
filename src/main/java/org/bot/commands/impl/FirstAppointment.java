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
import org.bot.utils.EditText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@BotCommand(name = "first_appointment")
public class FirstAppointment extends Command {
    private static final Logger log = LogManager.getLogger(FirstAppointment.class);
    private static final AppointmentsManager DATES_MANAGER = AppointmentsManager.getInstance();

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
        showKeyboard(getResource("command.firstAppointment.selectType"), keyboard.create());
    }

    @Override
    public void processCallbackQuery(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String query = callbackQuery.getData();
        AppointmentDate.Type type = AppointmentDate.Type.valueOf(query);
        AppointmentDate appointmentDate = DATES_MANAGER.getFirstAvailableDates(type);
        String firstAppointment = datesInfoToString(type, appointmentDate);
        sendOrEditLast(firstAppointment);
        ignoreCallback(callbackQuery);
    }

    private String datesInfoToString(AppointmentDate.Type type, AppointmentDate appointmentDate) {
        StringBuilder result = new StringBuilder();
        result.append(EditText.bold(type.name())).append(System.lineSeparator());
        if (appointmentDate == null) {
            result.append(getResource("command.dateInfo.noData"));
            return result.toString();
        }

        result.append(EditText.bold(appointmentDate.getDate())).append(": ");
        result.append(appointmentDate.getAvailableTime()).append(" ");
        long minutesAgo = appointmentDate.getTimeAfterUpdate();
        String timeAfterUpdate = EditText.timeAfterUpdate(minutesAgo, this);
        result.append(EditText.italic(timeAfterUpdate)).append(System.lineSeparator());
        return result.toString();
    }

    @Override
    public String getName() {
        return "first_appointment";
    }
}
