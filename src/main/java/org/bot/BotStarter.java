package org.bot;

import org.bot.appointment.AppointmentsManager;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Timer;
import java.util.TimerTask;

public class BotStarter {

    public static void main(String[] args) {
        String botName = null;
        String token = null;

        if (args != null && args.length > 1) {
            botName = args[0];
            token = args[1];
            System.out.println("Take name and token from args");
            System.out.println("name: " + botName + "; token: " + token);
        }

        if (botName == null || token == null) {
            botName = System.getenv("telegram_bot_name");
            token = System.getenv("telegram_bot_token");
            System.out.println("Take name and token env vars");
            System.out.println("name: " + botName + "; token: " + token);
        }

        if (botName == null || token == null)
            throw new RuntimeException("Bot name or token can't be null");

        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        //System.out.println("Testing jedis");

        Timer timer = new Timer(true);
        TimerTask timerTask = new JedisTimer();
        timer.schedule(timerTask, 10000, 10000);

        try {
            System.out.println("Starting bot");
            botsApi.registerBot(new WhereIsMyCardBot(botName, token));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static class JedisTimer extends TimerTask {

        //redis://redis3:6379/
        @Override
        public void run() {

            try {
                Jedis jedis = new Jedis("redis4", 6379);
                System.out.println("redis4");
                jedis.auth(System.getenv("redis_password"));
                jedis.setex("test", 100, "Some string");
                System.out.println(jedis.get("test"));
                jedis.close();
                System.out.println("Jedis test complete");
            } catch (JedisException e) {
                e.printStackTrace();
            }
        }
    }
}
