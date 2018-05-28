package org.bot.commands;

import org.bot.CommandParseException;
import org.bot.CommandResultHandler;
import org.telegram.telegrambots.api.objects.Update;

import java.util.List;

public abstract class Command {

    private boolean initialized = false;
    protected long chatId;
    protected List<String> commandArgs;

    public void initialize(Update update, List<String> commandArgs) {
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

    public List<String> getCommandArgs() {
        return commandArgs;
    }

    protected abstract void processInternal(CommandResultHandler handler);

    protected abstract void initializeInternal(Update update);
}

