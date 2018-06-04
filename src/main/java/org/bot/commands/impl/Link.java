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
    public void process(CommandResultHandler handler, Update update) {
        SendMessage message = new SendMessage();
        message.enableHtml(true);
        message.setChatId(update.getMessage().getChatId());
        message.setText(URL);
        handler.execute(message);
    }
}
