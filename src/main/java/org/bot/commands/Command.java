package org.bot.commands;

import org.telegram.telegrambots.api.objects.Update;

public abstract class Command extends Context {
    public static final String IGNORE_QUERY = "ignore";
    protected boolean awaitUserInput = false;

    public Command(CommandResultHandler handler, Update update) {
        super(handler, update);
    }

    public abstract String getName();

    public boolean isAwaitUserInput() {
        return awaitUserInput;
    }

    public abstract void process(Update update);

    public void processCallbackQuery(Update update) {
    }

    public void processUserInput(Update update) {
    }
}

