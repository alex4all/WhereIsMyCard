package org.bot.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Locale;
import java.util.ResourceBundle;

public abstract class Context {
    private static final Logger log = LogManager.getLogger(Context.class);
    private static final String RESOURCES = "messages";

    private CommandResultHandler handler;
    private ResourceBundle messages;
    private Message keyboardMessage;
    private Message lastBotMessage;

    private Long chatId;
    private User user;
    private Locale locale;

    public Context(CommandResultHandler handler, Update update) {
        this.handler = handler;
        chatId = update.getMessage().getChatId();
        user = update.getMessage().getFrom();
        String languageCode = update.getMessage().getFrom().getLanguageCode();
        log.info("languageCode: " + languageCode);
        locale = Locale.forLanguageTag(languageCode);
        ;
        log.info("locale: " + locale.toString());
        messages = ResourceBundle.getBundle(RESOURCES, locale, new UTF8Control());
    }

    public User getUser() {
        return user;
    }

    public String getResource(String id) {
        return messages.getString(id);
    }

    public Locale getLocale() {
        return locale;
    }

    public void showKeyboard(String text, InlineKeyboardMarkup keyboard) {
        if (keyboardMessage == null) {
            SendMessage message = new SendMessage()
                    .setChatId(chatId)
                    .setText(text)
                    .setReplyMarkup(keyboard);
            keyboardMessage = handler.execute(message);
        } else {
            EditMessageText message = new EditMessageText()
                    .setChatId(chatId)
                    .setText(text)
                    .setMessageId(keyboardMessage.getMessageId())
                    .setReplyMarkup(keyboard);
            handler.execute(message);
        }
    }

    public void sendOrEditLast(String text) {
        if (lastBotMessage == null) {
            SendMessage message = new SendMessage()
                    .enableHtml(true)
                    .setChatId(chatId)
                    .setText(text);
            lastBotMessage = handler.execute(message);
            return;
        }

        if (lastBotMessage.getText().equals(text)) {
            log.warn("Can't send same text as update");
            return;
        }

        EditMessageText message = new EditMessageText()
                .enableHtml(true)
                .setChatId(chatId)
                .setText(text)
                .setMessageId(lastBotMessage.getMessageId());
        handler.execute(message);
    }

    public Message sendMessage(String text) {
        SendMessage message = new SendMessage()
                .enableHtml(true)
                .setChatId(chatId)
                .setText(text);
        return handler.execute(message);
    }

    public void answerCallbackQuery(String text, CallbackQuery query) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(query.getId());
        answer.setShowAlert(false);
        answer.setText(text);
        handler.execute(answer);
    }

    public void ignoreCallback(CallbackQuery query) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(query.getId());
        answer.setShowAlert(false);
        handler.execute(answer);
    }

}
