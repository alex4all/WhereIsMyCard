package org.bot.commands.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.telegram.telegrambots.api.objects.Update;

@BotCommand(name = "start")
public class Start extends Command {
    private static final Logger log = LogManager.getLogger(Start.class);

    public Start(CommandResultHandler handler, Update update) {
        super(handler, update);
    }

    @Override
    public void process(Update update) {
        String userName = getUser().getFirstName();
        String startMessage = getResource("command.start.helloMessage");
        userName = userName == null ? getResource("command.start.helloMessage.defaultUser") : userName;
        startMessage = String.format(startMessage, userName);
        sendMessage(startMessage);
    }
}
