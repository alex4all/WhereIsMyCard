package org.bot.commands;

import org.telegram.telegrambots.api.objects.Update;

import java.util.List;

public abstract class Command {

    protected List<String> commandArgs;

    public void initialize(List<String> commandArgs) {
        this.commandArgs = commandArgs;
    }

    public abstract void process(CommandResultHandler handler, Update update);

    public abstract void processCallbackQuery(CommandResultHandler handler, Update update);

    public List<String> getCommandArgs() {
        return commandArgs;
    }

    public Long getChatId(Update update)
    {
        if(update.hasCallbackQuery())
            return update.getCallbackQuery().getMessage().getChatId();
        return update.getMessage().getChatId();
    }
}

