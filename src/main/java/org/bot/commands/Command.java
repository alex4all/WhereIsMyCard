package org.bot.commands;

import org.bot.CommandParseException;
import org.bot.CommandResultHandler;
import org.telegram.telegrambots.api.objects.Update;

public abstract class Command {

    private boolean initialized = false;
    protected long chatId;
    protected String[] commandArgs;

    public void initialize(Update update, String [] commandArgs) {
        chatId = update.getMessage().getChatId();
        this.commandArgs = commandArgs;
        initializeInternal(update);
        initialized = true;
    }

    public void process(CommandResultHandler handler) {
        if (!initialized)
            throw new CommandParseException(this.getClass().getName() + " is not initialized");
        processInternal(handler);
    }

    public long getChatId() {
        return chatId;
    }

    public String[] getCommandArgs() {
        return commandArgs;
    }

    protected abstract void processInternal(CommandResultHandler handler);

    protected abstract void initializeInternal(Update update);
}

