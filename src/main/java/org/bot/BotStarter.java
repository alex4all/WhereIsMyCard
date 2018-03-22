package org.bot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class BotStarter {

    public static void main(String[] args) {
        String token = args[0];
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            botsApi.registerBot(new WhereIsMyCardBot(token));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
