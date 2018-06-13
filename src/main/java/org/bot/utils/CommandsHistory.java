package org.bot.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bot.commands.Command;

import java.util.HashMap;
import java.util.Map;

public class CommandsHistory {
    private static final Logger log = LogManager.getLogger(CommandsHistory.class);
    private Map<String, Command> lastUserCommand = new HashMap<>();

    public CommandsHistory() {
    }

    public void putCommand(Command command, Long chatId, Integer userId) {
        String commandId = getId(chatId, userId);
        log.info("put command to cache: " + commandId);
        lastUserCommand.put(commandId, command);
    }

    public Command getCommand(Long chatId, Integer userId) {
        String commandId = getId(chatId, userId);
        log.info("get command from cache: " + commandId);
        return lastUserCommand.get(commandId);
    }

    public String getId(Long chatId, Integer userId) {
        return new StringBuilder("chat_").append(chatId).append("user_").append(userId).toString();
    }
}
