package org.bot.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.telegram.telegrambots.api.objects.Update;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CommandsManager {
    private static final Logger log = LogManager.getLogger(CommandsManager.class);
    private Map<String, Constructor<Command>> commandsByName = new HashMap<>();

    public CommandsManager() {
        Reflections reflections = new Reflections("org.bot.commands");
        Set<Class<?>> commands = reflections.getTypesAnnotatedWith(BotCommand.class);
        for (Class<?> command : commands) {
            String commandName = command.getDeclaredAnnotation(BotCommand.class).name();
            try {
                Constructor<Command> constructor = (Constructor<Command>) command.getConstructor(CommandResultHandler.class, Update.class);
                commandsByName.put(commandName, constructor);
            } catch (ClassCastException | NoSuchMethodException e) {
                log.error("Can't find constructor with Update argument for following command: " + commandName, e);
            }
        }
        log.info("List of available commands: " + commandsByName.keySet());
    }

    public Command createCommand(CommandResultHandler handler, Update update) throws UnknownCommandException {
        String message = update.getMessage().getText();
        String commandName = getCommandName(message);
        Constructor<Command> commandConstructor = commandsByName.get(commandName);
        if (commandConstructor == null)
            throw new UnknownCommandException();
        try {
            return commandConstructor.newInstance(handler, update);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Can't create instance of command: " + commandName, e);
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
