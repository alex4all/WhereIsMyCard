package org.bot.commands.impl;

import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.bot.utils.MessageUtils;
import org.telegram.telegrambots.api.objects.Update;

@BotCommand(name = "link")
public class Link extends Command {

    private static final String URL = "http://webqms.pl/puw/index.php";

    @Override
    public void process(CommandResultHandler handler, Update update) {
        MessageUtils.sendMessage(handler, update, URL);
    }
}
