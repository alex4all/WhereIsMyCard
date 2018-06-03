package org.bot.utils;

import org.telegram.telegrambots.api.objects.Update;

public class UpdateToID {

    public static String callbackQuery(Update update) {
        StringBuilder builder = new StringBuilder();
        builder.append("user:").append(update.getCallbackQuery().getFrom().getId()).append(";");
        builder.append("chat:").append(update.getCallbackQuery().getMessage().getChatId());
        return builder.toString();
    }

    public static String message(Update update) {
        StringBuilder builder = new StringBuilder();
        builder.append("user:").append(update.getMessage().getFrom().getId()).append(";");
        builder.append("chat:").append(update.getMessage().getChatId());
        return builder.toString();
    }
}
