package org.bot.commands;

import org.bot.AppData;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;

public abstract class Command {
    private static AppData appData = AppData.getInstance();
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

    protected String getResource(String resource) {
        return appData.getMessage(resource);
    }

    public Long getChatId(Update update) {
        if (update.hasCallbackQuery())
            return update.getCallbackQuery().getMessage().getChatId();
        return update.getMessage().getChatId();
    }
}

