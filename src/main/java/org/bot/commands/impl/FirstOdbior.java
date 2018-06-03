package org.bot.commands.impl;

import org.bot.appointment.AppointmentDate;
import org.bot.appointment.AppointmentDatesManager;
import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandParseException;
import org.bot.commands.CommandResultHandler;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;

import java.util.List;


@BotCommand(name = "first_odbior")
public class FirstOdbior extends Command {
    private static final AppointmentDatesManager DATES_MANAGER = AppointmentDatesManager.getInstance();
    private int daysCount = 1;

    @Override
    public void process(CommandResultHandler handler, Update update) {
        List<AppointmentDate> datesInfo = DATES_MANAGER.getFirstAvailableDates(AppointmentDate.Type.ODBIOR, daysCount);
        SendMessage message = new SendMessage();
        message.enableHtml(true);
        message.setChatId(update.getMessage().getChatId());
        message.setText(datesInfoToString(datesInfo));
        handler.execute(message);
    }

    @Override
    public void processCallbackQuery(CommandResultHandler handler, Update update) {

    }

    private String datesInfoToString(List<AppointmentDate> datesInfo) {
        if (datesInfo.size() == 0) {
            return "No data found";
        }
        StringBuilder result = new StringBuilder();
        for (AppointmentDate dayInfo : datesInfo)
            result.append(dayInfo.toMessageWithDate()).append(System.lineSeparator());
        return result.toString();
    }

}
