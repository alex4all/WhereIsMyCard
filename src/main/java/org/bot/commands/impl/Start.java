package org.bot.commands.impl;

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
        return "Hello, " + userName + "! Welcome to community of peoples who waiting for \"karta pobyitu\". " +
                "I can help you to get information about available dates in Urzand. Maybe someday I also will ba able to" +
                " give you this card for money =)";
    }
}
