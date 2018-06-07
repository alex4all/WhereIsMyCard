package org.bot.commands.impl;

import org.bot.appointment.AppointmentDate;
import org.bot.appointment.AppointmentsManager;
import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.bot.keyboards.HorizontalKeyboard;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;

import java.util.List;

@BotCommand(name = "first_appointment")
public class FirstAppointment extends Command {
    private static final AppointmentsManager DATES_MANAGER = AppointmentsManager.getInstance();

    @Override
    public void process(CommandResultHandler handler, Update update) {
        HorizontalKeyboard keyboard = new HorizontalKeyboard()
                .elements(AppointmentDate.Type.values())
                .callbackPrefix("");
        System.out.println("Send new keyboard");
        SendMessage message = new SendMessage()
                .enableHtml(true)
                .setChatId(getChatId(update))
                .setText("Select appointment type")
                .setReplyMarkup(keyboard.create());
        handler.execute(message);
    }

    public void processCallbackQuery(CommandResultHandler handler, Update update) {
        String callbackQuery = update.getCallbackQuery().getData();
        AppointmentDate.Type type = AppointmentDate.Type.valueOf(callbackQuery);
        List<AppointmentDate> datesInfo = DATES_MANAGER.getFirstAvailableDates(type, 1);
        String firstAppointment = datesInfoToString(datesInfo);
        sendOrEdit(handler, update, firstAppointment);
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
