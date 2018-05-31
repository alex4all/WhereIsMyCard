package org.bot.commands;

import org.reflections.Reflections;
import org.telegram.telegrambots.api.objects.Update;

import java.util.*;

public class CommandsManager {

    Map<String, Class<Command>> commandsByName = new HashMap<>();

    public CommandsManager() {
        Reflections reflections = new Reflections("org.bot.commands");
        Set<Class<?>> commands = reflections.getTypesAnnotatedWith(BotCommand.class);
        for (Class<?> command : commands) {
            String commandName = command.getDeclaredAnnotation(BotCommand.class).name();
            commandsByName.put(commandName, (Class<Command>) command);
        }
        System.out.println("List of available commands: " + commandsByName.keySet());
    }

    public Command createCommand(Update update) {
        String message = update.getMessage().getText();
        String commandName = getCommandName(message);
        if (!commandsByName.containsKey(commandName))
            throw new CommandParseException("Command doesn't exists: " + commandName);
        try {
            Command command = commandsByName.get(commandName).newInstance();
            command.initialize(update, getCommandArgs(message, commandName));
            return command;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new CommandParseException("Can't process command: " + commandName);
        }
    }

    public static String getCommandName(String message) {
        // /command
        if (message.indexOf(' ') == -1 && message.indexOf('@') == -1)
            return message.substring(1);
        // /command@botname
        if (message.indexOf(' ') == -1 && message.indexOf('@') != -1)
            return message.substring(1, message.indexOf('@'));
        // /command arg1 arg2
        // /command arg1 arg2@botname
        return message.substring(1, message.indexOf(' '));
    }

    public static List<String> getCommandArgs(String message, String commandName) {
        // /command arg1 arg2 -> arg1 arg2
        String args = message.substring(message.indexOf(commandName) + commandName.length());

        if (args.toUpperCase().endsWith("BOT")) {
            // arg1 arg2@MyBot
            if (args.lastIndexOf(' ') < args.lastIndexOf('@'))
                args = args.substring(0, args.lastIndexOf('@'));
        }
        args = args.trim();
        if(args.length() == 0)
            return new ArrayList<>(1);
        // arg1 arg2 -> [arg1, arg2]
        return Arrays.asList(args.trim().split(" "));
    }
}
