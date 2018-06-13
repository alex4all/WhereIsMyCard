package org.bot.commands.impl;

import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.telegram.telegrambots.api.objects.Update;

@BotCommand(name = "make_appointment")
public class MakeAppointment extends Command {

    public MakeAppointment(CommandResultHandler handler, Update update) {
        super(handler, update);
    }

    @Override
    public void process(Update update) {

    }

    @Override
    public String getName() {
        return "make_appointment";
    }
}
