package org.bot.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class CommandResultHandler {
    private static final Logger log = LogManager.getLogger(CommandResultHandler.class);
    private AbsSender sender;

    public CommandResultHandler(AbsSender sender) {
        this.sender = sender;
    }

    public Message execute(SendMessage message) {
        try {
            return sender.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void execute(EditMessageText message) {
        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error(e);
        }
    }

    public void execute(AnswerCallbackQuery answerCallbackQuery) {
        try {
            sender.execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            log.error(e);
        }
    }
}
