package org.bot.commands.impl;

import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;

@BotCommand(name = "link")
public class Link extends Command {

    private static final String URL = "http://webqms.pl/puw/index.php";

    @Override
    protected void processInternal(CommandResultHandler handler) {
        SendMessage message = new SendMessage();
        message.enableHtml(true);
        message.setChatId(getChatId());
        message.setText(URL);
        handler.execute(message);
    }

    @Override
    protected void initializeInternal(Update update) {
    }
}
