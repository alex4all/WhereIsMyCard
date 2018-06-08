package org.bot.commands;

import org.reflections.Reflections;
import org.telegram.telegrambots.api.objects.Update;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CommandsManager {

    private Map<String, Class<Command>> commandsByName = new HashMap<>();

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
            throw new CommandParseException("Unrecognized command. Say what?");
        try {
            return commandsByName.get(commandName).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new CommandParseException("Unrecognized command. Say what?");
        }
    }

    private static String getCommandName(String message) {
        // /command
        if (message.indexOf(' ') == -1 && message.indexOf('@') == -1)
            return message.substring(1);
        // /command@botname
        return message.substring(1, message.indexOf('@'));
    }
}
