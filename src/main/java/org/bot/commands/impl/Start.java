package org.bot.commands.impl;

import org.bot.AppData;
import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.bot.utils.MessageUtils;
import org.telegram.telegrambots.api.objects.Update;

@BotCommand(name = "start")
public class Start extends Command {
    @Override
    public void process(CommandResultHandler handler, Update update) {
        String startMessage = getStartMessage(update.getMessage().getChat().getFirstName());
        MessageUtils.sendMessage(handler, update, startMessage);
    }

    private String getStartMessage(String userName) {
        String helloMessage = getResource("command.start.helloMessage");
        userName = userName == null ? getResource("command.start.helloMessage.defaultUser") : userName;
        return String.format(helloMessage, userName);
    }
}
