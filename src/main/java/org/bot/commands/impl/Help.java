package org.bot.commands.impl;

import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.telegram.telegrambots.api.objects.Update;

@BotCommand(name = "help")
public class Help extends Command {

    public Help(CommandResultHandler handler, Update update) {
        super(handler, update);
    }

    @Override
    public void process(Update update) {
        sendMessage(generateHelpMessage());
    }

    private String generateHelpMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append("<b>/first_appointment</b> - get first available appointment date").append(System.lineSeparator());
        builder.append("<b>/date_info</b> - get information about selected date").append(System.lineSeparator());
        builder.append("<b>/link</b> - get Urzad URL").append(System.lineSeparator());
        builder.append("<b>/github</b> - source code").append(System.lineSeparator());
        builder.append("<b>/help</b> - list of available commands");
        return builder.toString();
    }

}
