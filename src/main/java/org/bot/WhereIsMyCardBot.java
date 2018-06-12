package org.bot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bot.appointment.AppointmentsManager;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.bot.commands.CommandsManager;
import org.bot.commands.UnknownCommandException;
import org.bot.utils.CommandsHistory;
import org.bot.utils.UpdateToID;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class WhereIsMyCardBot extends TelegramLongPollingBot {
    private static final Logger log = LogManager.getLogger(WhereIsMyCardBot.class);
    private final AppointmentsManager datesManager;
    private final String botName;
    private final String token;
    private final CommandsManager commandsManager;

    public WhereIsMyCardBot(String botName, String token) {
        this.botName = botName;
        this.token = token;
        datesManager = AppointmentsManager.getInstance();
        commandsManager = new CommandsManager();
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
            String message = update.getMessage().getText();
            if (isCommand(message)) {
                try {
                    Command command = commandsManager.createCommand(new CommandResultHandler(this), update);
                    CommandsHistory.getInstance().putCommand(UpdateToID.message(update), command);
                    command.process(update);
                    return;
                } catch (UnknownCommandException e) {
                    log.error("Command not found: " + e.getCommandName(), e);
                    sendError(e.getMessage(), update.getMessage().getChatId());
                }
            }

            Command command = CommandsHistory.getInstance().getCommand(UpdateToID.message(update));
            if (command != null && command.isAwaitUserInput()) {
                command.processUserInput(update);
            }
        }

        // process callback query
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackquery = update.getCallbackQuery();
            if (Command.IGNORE_QUERY.equals(callbackquery.getData()))
                ignoreQuery(callbackquery);
            log.info("Callback: " + update.getCallbackQuery().getData());
            Command command = CommandsHistory.getInstance().getCommand(UpdateToID.callbackQuery(update));
            if (command != null /*&& command.getUserId() == update.getCallbackQuery().getFrom().getId()*/) {
                command.processCallbackQuery(update);
            }
//            else {
//                String name = update.getCallbackQuery().getFrom().getFirstName();
//                String message = "Sorry, " + name + ", but looks like your command is expired. " +
//                        "Try to process it one more time";
//                sendError(message, update.getCallbackQuery().getMessage().getChatId());
//            }
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
