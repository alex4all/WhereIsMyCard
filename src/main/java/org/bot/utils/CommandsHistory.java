package org.bot.utils;

import org.bot.commands.Command;

import java.util.HashMap;
import java.util.Map;

public class CommandsHistory {
    private Map<String, Command> stackByChat = new HashMap<>();
    private static final CommandsHistory INSTANCE = new CommandsHistory();

    private CommandsHistory() {
    }

    public static CommandsHistory getInstance() {
        return INSTANCE;
    }

    public void putCommand(String commandId, Command command) {
        System.out.println("put command to cache: " + commandId);
        stackByChat.put(commandId, command);
    }

    public Command getCommand(String commandId) {
        System.out.println("get command from cache: " + commandId);
        return stackByChat.get(commandId);
    }
}
