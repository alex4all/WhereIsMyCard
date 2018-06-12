package org.bot.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bot.commands.Command;

import java.util.HashMap;
import java.util.Map;

public class CommandsHistory {
    private static final Logger log = LogManager.getLogger(CommandsHistory.class);
    private Map<String, Command> stackByChat = new HashMap<>();
    private static final CommandsHistory INSTANCE = new CommandsHistory();

    private CommandsHistory() {
    }

    public static CommandsHistory getInstance() {
        return INSTANCE;
    }

    public void putCommand(String commandId, Command command) {
        log.info("put command to cache: " + commandId);
        stackByChat.put(commandId, command);
    }

    public Command getCommand(String commandId) {
        log.info("get command from cache: " + commandId);
        return stackByChat.get(commandId);
    }
}
