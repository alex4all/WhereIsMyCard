package org.bot.commands;

public class UnknownCommandException extends Exception {

    private String commandName;

    public UnknownCommandException() {
        super();
    }

    public UnknownCommandException(String commandName) {
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }
}
