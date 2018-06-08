package org.bot.utils;

import org.bot.commands.CommandResultHandler;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;

public class MessageUtils {
    public static Message sendOrEdit(Message lastMessage, Long chatId, String text, InlineKeyboardMarkup keyboard, CommandResultHandler handler) {
        if (lastMessage == null) {
            SendMessage message = new SendMessage()
                    .setChatId(chatId)
                    .setText(text)
                    .setReplyMarkup(keyboard);
            return handler.execute(message);
        } else {
            EditMessageText message = new EditMessageText()
                    .setChatId(chatId)
                    .setText(text)
                    .setMessageId(lastMessage.getMessageId())
                    .setReplyMarkup(keyboard);
            handler.execute(message);
            return lastMessage;
        }
    }

    public static Message sendOrEdit(Message lastMessage, Long chatId, String text, CommandResultHandler handler) {
        if (lastMessage == null) {
            SendMessage message = new SendMessage()
                    .enableHtml(true)
                    .setChatId(chatId)
                    .setText(text);
            return handler.execute(message);
        } else {
            EditMessageText message = new EditMessageText()
                    .enableHtml(true)
                    .setChatId(chatId)
                    .setText(text)
                    .setMessageId(lastMessage.getMessageId());
            handler.execute(message);
            return lastMessage;
        }
    }

    public static Message sendOrEdit(Message lastMessage, CommandResultHandler handler, Update update, String text) {
        long chatId;
        if (update.hasCallbackQuery())
            chatId = update.getCallbackQuery().getMessage().getChatId();
        else
            chatId = update.getMessage().getChatId();

        if (lastMessage == null) {
            SendMessage message = new SendMessage()
                    .enableHtml(true)
                    .setChatId(chatId)
                    .setText(text);
            return handler.execute(message);
        } else {
            EditMessageText message = new EditMessageText()
                    .enableHtml(true)
                    .setChatId(chatId)
                    .setMessageId(lastMessage.getMessageId())
                    .setText(text);
            handler.execute(message);
            return lastMessage;
        }
    }

    public static Message sendMessage(CommandResultHandler handler, Update update, String text) {
        long chatId;
        if (update.hasCallbackQuery())
            chatId = update.getCallbackQuery().getMessage().getChatId();
        else
            chatId = update.getMessage().getChatId();
        SendMessage message = new SendMessage()
                .enableHtml(true)
                .setChatId(chatId)
                .setText(text);
        return handler.execute(message);
    }

    public static void edit(CommandResultHandler handler, Message message, String text) {
        EditMessageText edit = new EditMessageText()
                .enableHtml(true)
                .setChatId(message.getChatId())
                .setMessageId(message.getMessageId())
                .setText(text);
        handler.execute(edit);
    }

    public static Message sendMessage(CommandResultHandler handler, Update update, String text, InlineKeyboardMarkup keyboard) {
        long chatId;
        if (update.hasCallbackQuery())
            chatId = update.getCallbackQuery().getMessage().getChatId();
        else
            chatId = update.getMessage().getChatId();
        SendMessage message = new SendMessage()
                .enableHtml(true)
                .setChatId(chatId)
                .setReplyMarkup(keyboard)
                .setText(text);
        return handler.execute(message);
    }
}
