package org.bot.commands.impl;

import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.telegram.telegrambots.api.objects.Update;

@BotCommand(name = "github")
public class GitHub extends Command {
    private static final String URL = "https://github.com/alex4all/WhereIsMyCard";

    @Override
    public void process(CommandResultHandler handler, Update update) {
        sendMessage(handler, update, URL);
    }
}
