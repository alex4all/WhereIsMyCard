package org.bot.commands.impl;

import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.telegram.telegrambots.api.objects.Update;

@BotCommand(name = "link")
public class Link extends Command {

    private static final String URL = "http://webqms.pl/puw/index.php";

    public Link(CommandResultHandler handler, Update update) {
        super(handler, update);
    }

    @Override
    public void process(Update update) {
        sendMessage(URL);
    }

    @Override
    public String getName() {
        return "link";
    }
}
