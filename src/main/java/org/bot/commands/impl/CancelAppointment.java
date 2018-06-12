package org.bot.commands.impl;

import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.telegram.telegrambots.api.objects.Update;

@BotCommand(name = "cancel_appointment")
public class CancelAppointment extends Command {


    public CancelAppointment(CommandResultHandler handler, Update update) {
        super(handler, update);
    }

    @Override
    public void process(Update update) {

    }

    @Override
    public void processCallbackQuery(Update update) {

    }
}
