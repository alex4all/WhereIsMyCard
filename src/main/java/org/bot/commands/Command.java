package org.bot.commands;

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
}

