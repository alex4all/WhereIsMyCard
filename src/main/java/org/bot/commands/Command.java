package org.bot.commands;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;

public abstract class Command {
    protected int userId;
    protected Message lastBotMessage;
    protected boolean awaitUserInput = false;

    public boolean isAwaitUserInput() {
        return awaitUserInput;
    }

    public void setAwaitUserInput(boolean awaitUserInput) {
        this.awaitUserInput = awaitUserInput;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public abstract void process(CommandResultHandler handler, Update update);

    public void processCallbackQuery(CommandResultHandler handler, Update update) {
    }

    public void processUserInput(CommandResultHandler handler, Update update) {
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

