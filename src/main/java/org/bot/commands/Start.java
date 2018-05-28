package org.bot.commands;

import org.bot.CommandResultHandler;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;

@BotCommand(name = "start")
public class Start extends Command {
    private String userName;

    @Override
    protected void processInternal(CommandResultHandler handler) {
        SendMessage message = new SendMessage();
        message.enableHtml(true);
        message.setChatId(getChatId());
        message.setText(getStartMessage());
        handler.execute(message);
    }

    @Override
    protected void initializeInternal(Update update) {
        userName = update.getMessage().getChat().getFirstName();
    }

    private String getStartMessage() {
        return "Привет, " + userName + "! Добро пожаловать в сообщество людей, отчаявщихся получить карту побыту. " +
                "Я помогу тебе получить информацию о доступных талончиках в ужонде";
    }
}
