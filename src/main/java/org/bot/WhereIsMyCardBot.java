package org.bot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bot.appointment.AppointmentsManager;
import org.bot.commands.*;
import org.bot.commands.impl.Help;
import org.bot.utils.CommandsHistory;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.Locale;

public class WhereIsMyCardBot extends TelegramLongPollingBot {
    private static final Logger log = LogManager.getLogger(WhereIsMyCardBot.class);
    private final AppointmentsManager datesManager;
    private final String botName;
    private final String token;
    private final CommandsManager commandsManager = new CommandsManager();
    private final CommandsHistory commandsHistory = new CommandsHistory();

    public WhereIsMyCardBot(String botName, String token) {
        this.botName = botName;
        this.token = token;
        datesManager = AppointmentsManager.getInstance();
        log.info("Bot started");
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.debug("onUpdateReceived: " + update.toString());
        // process message
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String messageText = message.getText();
            if (isCommand(messageText)) {
                try {
                    Command command = commandsManager.createCommand(new CommandResultHandler(this), update);
                    commandsHistory.putCommand(command, message.getChatId(), message.getFrom().getId());
                    command.process(update);
                    return;
                } catch (UnknownCommandException e) {
                    log.error("Command not found: " + e.getCommandName(), e);
                    sendError(e.getMessage(), update.getMessage().getChatId());
                }
            }

            Command command = commandsHistory.getCommand(message.getChatId(), message.getFrom().getId());
            if (command != null && command.isAwaitUserInput()) {
                command.processUserInput(update);
            } else {
                Help help = new Help(new CommandResultHandler(this), update);
                help.process(update);
            }
        }

        // process callback query
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackquery = update.getCallbackQuery();
            if (Command.IGNORE_QUERY.equals(callbackquery.getData()))
                ignoreQuery(callbackquery);
            log.info("Callback: " + update.getCallbackQuery().getData());
            Message message = callbackquery.getMessage();
            Command command = commandsHistory.getCommand(message.getChatId(), callbackquery.getFrom().getId());
            // user don't have active commands
            if (command == null || command.getKeyboardMessage() == null) {
                String languageCode = callbackquery.getFrom().getLanguageCode();
                Locale locale = Locale.forLanguageTag(languageCode);
                String noActiveCommands = Context.getResources(locale).getString("bot.notification.noActiveInterfaces");
                errorQuery(noActiveCommands, callbackquery);
                return;
            }

            boolean clickOnActiveKeyboard = message.getMessageId().equals(command.getKeyboardMessage().getMessageId());
            if (!clickOnActiveKeyboard) {
                String languageCode = callbackquery.getFrom().getLanguageCode();
                Locale locale = Locale.forLanguageTag(languageCode);
                String anotherCommandActive = Context.getResources(locale).getString("bot.notification.anotherInterfaceActive");
                errorQuery(anotherCommandActive + " " + command.getName(), callbackquery);
                return;
            }
            command.processCallbackQuery(update);
        }
    }

    public void ignoreQuery(CallbackQuery callbackquery) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackquery.getId());
        try {
            execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            log.error("Exception occurred in ignoreQuery method call", e);
        }
    }

    public void errorQuery(String error, CallbackQuery callbackquery) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackquery.getId());
        answerCallbackQuery.setText(error);
        try {
            execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            log.error("Exception occurred in ignoreQuery method call", e);
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
            log.error("Can't send error message", e);
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
