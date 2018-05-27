package org.bot.commands;

import org.bot.CommandParseException;
import org.bot.CommandResultHandler;
import org.telegram.telegrambots.api.objects.Update;

public abstract class Command {

    private boolean initialized = false;
    protected long chatId;
    protected String commandBody;

    public void initialize(Update update, String commandBody) {
        chatId = update.getMessage().getChatId();
        this.commandBody = commandBody;
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

    public String getCommandBody() {
        return commandBody;
    }

    protected abstract void processInternal(CommandResultHandler handler);

    protected abstract void initializeInternal(Update update);
}

