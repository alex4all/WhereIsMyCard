package org.bot.commands.impl;

import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;

@BotCommand(name = "start")
public class Start extends Command {

    @Override
    public void process(CommandResultHandler handler, Update update) {
        SendMessage message = new SendMessage();
        message.enableHtml(true);
        message.setChatId(update.getMessage().getChatId());
        message.setText(getStartMessage(update.getMessage().getChat().getFirstName()));
        handler.execute(message);
    }

    @Override
    public void processCallbackQuery(CommandResultHandler handler, Update update) {

    }

    private String getStartMessage(String userName) { //TODO
        return "Hello, " + userName + "! Welcome to community of peoples who waiting for \"karta pobyitu\". " +
                "I can help you to get information about available dates in Urzand. Maybe someday I also will ba able to" +
                " give you this card for money =)";
    }
}
