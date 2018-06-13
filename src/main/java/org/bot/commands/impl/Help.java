package org.bot.commands.impl;

import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.bot.utils.EditText;
import org.telegram.telegrambots.api.objects.Update;

@BotCommand(name = "help")
public class Help extends Command {
    private static final String FIRST_APPOINTMENT = EditText.bold("/first_appointment");
    private static final String DATE_INFO = EditText.bold("/date_info");
    private static final String LINK = EditText.bold("/link");
    private static final String GITHUB = EditText.bold("/github");
    private static final String HELP = EditText.bold("/help");

    public Help(CommandResultHandler handler, Update update) {
        super(handler, update);
    }

    @Override
    public void process(Update update) {
        String help = new StringBuilder()
                .append(FIRST_APPOINTMENT).append(" ").append(getResource("command.help.firstAppointment")).append(System.lineSeparator())
                .append(DATE_INFO).append(" ").append(getResource("command.help.dateInfo")).append(System.lineSeparator())
                .append(LINK).append(" ").append(getResource("command.help.link")).append(System.lineSeparator())
                .append(GITHUB).append(" ").append(getResource("command.help.github")).append(System.lineSeparator())
                .append(HELP).append(" ").append(getResource("command.help.help")).append(System.lineSeparator())
                .toString();
        sendMessage(help);
    }

    @Override
    public String getName() {
        return "help";
    }
}
