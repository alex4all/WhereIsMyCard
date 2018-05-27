package org.bot;

import org.bot.commands.Command;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

public class WhereIsMyCardBot extends TelegramLongPollingBot {

    public static final String BOT_NAME = "@WhereIsMyCardBot";


    private final AppointmentDatesManager datesManager;

    private final String botName;
    private final String token;
    private final CommandsManager commandsManager;

    public WhereIsMyCardBot(String botName, String token) {
        this.botName = botName;
        this.token = token;
        datesManager = AppointmentDatesManager.getInstance();
        commandsManager = new CommandsManager();
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            if (isCommand(message)) {
                Command command = commandsManager.createCommand(update);
                command.process(new CommandResultHandler(this));
                return;
            }
        }
    }

    public boolean isCommand(String message) {
        return message.startsWith("/");
    }

    @Override
    public String getBotToken() {
        return token;
    }
}
