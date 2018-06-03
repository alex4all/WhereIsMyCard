package org.bot.commands.impl;

import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.telegram.telegrambots.api.objects.Update;

@BotCommand(name = "cancel")
public class CancelAppointment extends Command {

    @Override
    public void process(CommandResultHandler handler, Update update) {

    }

    @Override
    public void processCallbackQuery(CommandResultHandler handler, Update update) {

    }
}
