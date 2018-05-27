package org.bot.commands;

import org.bot.*;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.bot.AppointmentDatesManager.DATE_PATTERN;

@BotCommand(name = "date_info")
public class DateInfo extends Command {

    private Date date;
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);

    @Override
    protected void processInternal(CommandResultHandler handler) {
        List<AppointmentDate> dateInfo;
        try {
            dateInfo = AppointmentDatesManager.getInstance().getDateInfo(date);
        } catch (ParseException e) {
            throw new CommandParseException(e.getMessage());
        }
        SendMessage message = new SendMessage();
        message.enableHtml(true);
        message.setChatId(getChatId());
        message.setText(dateInfoToString(dateInfo));
        handler.execute(message);
    }

    @Override
    protected void initializeInternal(Update update) {
        List<String> args = CommandsManager.getCommandArgs(commandBody);
        if (args.size() != 1)
            throw new CommandParseException("Incorrect count of args. You have to provide date using following pattern: " + DATE_PATTERN);
        String dateToCheck = args.get(0);
        try {
            date = dateFormat.parse(dateToCheck);
        } catch (ParseException e) {
            throw new CommandParseException("You have to provide date using following pattern: " + DATE_PATTERN);
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
            return "No data found for provided date: " + dateFormat.format(date);
        }

        StringBuilder result = new StringBuilder();
        result.append("<b>").append(verifiedDates.get(0).getDate()).append(":</b>").append(System.lineSeparator());

        for (AppointmentDate appDate : dateInfo)
            result.append(appDate.toMessageWithType()).append(System.lineSeparator());
        return result.toString();
    }
}
