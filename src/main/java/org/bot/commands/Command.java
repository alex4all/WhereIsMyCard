package org.bot.commands;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;

import java.util.List;

public abstract class Command {

    protected List<String> commandArgs;

    protected int userId;

    protected Message lastBotMessage;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void initialize(List<String> commandArgs) {
        this.commandArgs = commandArgs;
    }

    public abstract void process(CommandResultHandler handler, Update update);

    public void processCallbackQuery(CommandResultHandler handler, Update update) {

    }

    public List<String> getCommandArgs() {
        return commandArgs;
    }

    public Long getChatId(Update update) {
        if (update.hasCallbackQuery())
            return update.getCallbackQuery().getMessage().getChatId();
        return update.getMessage().getChatId();
    }

    protected void sendOrEdit(CommandResultHandler handler, Update update, String text) {
        long chatId;
        if (update.hasCallbackQuery())
            chatId = update.getCallbackQuery().getMessage().getChatId();
        else
            chatId = update.getMessage().getChatId();

        if (lastBotMessage == null) {
            SendMessage message = new SendMessage()
                    .enableHtml(true)
                    .setChatId(chatId)
                    .setText(text);
            lastBotMessage = handler.execute(message);
        } else {
            EditMessageText message = new EditMessageText()
                    .enableHtml(true)
                    .setChatId(chatId)
                    .setMessageId(lastBotMessage.getMessageId())
                    .setText(text);
            handler.execute(message);
        }
    }

    protected void sendMessage(CommandResultHandler handler, Update update, String text) {
        long chatId;
        if (update.hasCallbackQuery())
            chatId = update.getCallbackQuery().getMessage().getChatId();
        else
            chatId = update.getMessage().getChatId();
        SendMessage message = new SendMessage()
                .enableHtml(true)
                .setChatId(chatId)
                .setText(text);
        lastBotMessage = handler.execute(message);
    }
}

