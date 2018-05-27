package org.bot;

import org.bot.commands.BotCommand;
import org.bot.commands.Command;
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
    }

    public Command createCommand(Update update) {
        String message = update.getMessage().getText();
        String commandBody = getCommandBody(message);
        String commandName = getCommandName(commandBody);
        if (!commandsByName.containsKey(commandName))
            throw new CommandParseException("Command doesn't exists: " + commandName);
        try {
            Command command = commandsByName.get(commandName).newInstance();
            command.initialize(update, commandBody);
            return command;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new CommandParseException("Can't process command: " + commandName);
        }
    }

    public static String getCommandBody(String command) {
        if (command.endsWith(WhereIsMyCardBot.BOT_NAME)) {
            return command.substring(1, command.length() - WhereIsMyCardBot.BOT_NAME.length());
        }
        return command.substring(1);
    }

    public static String getCommandName(String commandBody) {
        return commandBody.split(" ")[0];
    }

    public static List<String> getCommandArgs(String commandBody) {
        List<String> list = Arrays.asList(commandBody.split(" "));
        if (list.size() > 1) {
            // remove command name from list
            return list.subList(1, list.size());
        } else return new ArrayList<>();
    }
}
