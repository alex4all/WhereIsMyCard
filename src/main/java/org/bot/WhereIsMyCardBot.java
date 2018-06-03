package org.bot;

import org.bot.appointment.AppointmentDatesManager;
import org.bot.commands.Command;
import org.bot.commands.CommandParseException;
import org.bot.commands.CommandResultHandler;
import org.bot.commands.CommandsManager;
import org.bot.utils.CommandsHistory;
import org.bot.utils.UpdateToID;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class WhereIsMyCardBot extends TelegramLongPollingBot {

    private final AppointmentDatesManager datesManager;
    private final String botName;
    private final String token;
    private final CommandsManager commandsManager;

    public WhereIsMyCardBot(String botName, String token) {
        this.botName = botName;
        this.token = token;
        datesManager = AppointmentDatesManager.getInstance();
        commandsManager = new CommandsManager();
        System.out.println("Bot started");
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            System.out.println("Callback: " + update.getCallbackQuery().getData());

            Command command = CommandsHistory.getInstance().getCommand(UpdateToID.callbackQuery(update));
            if (command != null)
                command.processCallbackQuery(new CommandResultHandler(this), update);
            else
                sendError("Looks like you need to process command one more time =)",
                        update.getCallbackQuery().getMessage().getChatId());
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            System.out.println("Update received: " + message);
            if (isCommand(message)) {
                try {
                    Command command = commandsManager.createCommand(update);
                    CommandsHistory.getInstance().putCommand(UpdateToID.message(update), command);
                    command.process(new CommandResultHandler(this), update);
                    return;
                } catch (CommandParseException e) {
                    e.printStackTrace();
                    sendError(e.getMessage(), update.getMessage().getChatId());
                }
            }
        }
    }

    public void sendError(String error, long chatId) {
        SendMessage message = new SendMessage();
        message.enableHtml(true);
        message.setChatId(chatId);
        message.setText(error);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
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
