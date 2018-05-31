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

@BotCommand(name = "first_zlozenie")
public class FirstZlozenie extends Command {
    private static final AppointmentDatesManager DATES_MANAGER = AppointmentDatesManager.getInstance();
    private int daysCount;

    @Override
    protected void processInternal(CommandResultHandler handler) {
        List<AppointmentDate> datesInfo = DATES_MANAGER.getFirstAvailableDates(AppointmentDate.Type.ZLOZENIE, daysCount);

        SendMessage message = new SendMessage();
        message.enableHtml(true);
        message.setChatId(getChatId());
        message.setText(datesInfoToString(datesInfo));
        handler.execute(message);
    }

    @Override
    protected void initializeInternal(Update update) {
        if (commandArgs.size() == 0) {
            daysCount = 1;
            return;
        }

        try {
            daysCount = Integer.parseInt(commandArgs.get(0));
        } catch (NumberFormatException e) {
            throw new CommandParseException("Чего это ты мне подсунул? Это число по-твоему " + commandArgs.get(0) + "?");
        }
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
