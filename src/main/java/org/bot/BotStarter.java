package org.bot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class BotStarter {
    private static final Logger log = LogManager.getLogger(BotStarter.class);

    public static void main(String[] args) {
        String botName = null;
        String token = null;

        if (args != null && args.length > 1) {
            botName = args[0];
            token = args[1];
            log.info("Take name and token from args");
            log.info("name: " + botName + "; token: " + token);
        }

        if (botName == null || token == null) {
            botName = System.getenv("telegram_bot_name");
            token = System.getenv("telegram_bot_token");
            log.info("Take name and token env vars");
            log.info("name: " + botName + "; token: " + token);
        }

        if (botName == null || token == null)
            throw new RuntimeException("Bot name or token can't be null");

        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            log.info("Starting bot");
            botsApi.registerBot(new WhereIsMyCardBot(botName, token));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
