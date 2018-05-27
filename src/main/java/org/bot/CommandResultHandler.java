package org.bot;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class CommandResultHandler {

    private AbsSender sender;

    public CommandResultHandler(AbsSender sender) {
        this.sender = sender;
    }

    public void execute(SendMessage message) {
        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
