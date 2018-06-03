package org.bot.commands.impl;

import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;

@BotCommand(name = "help")
public class Help extends Command {

    @Override
    public void process(CommandResultHandler handler, Update update) {
        SendMessage message = new SendMessage();
        message.enableHtml(true);
        message.setChatId(update.getMessage().getChatId());
        message.setText(generateHelpMessage());
        handler.execute(message);
    }

    @Override
    public void processCallbackQuery(CommandResultHandler handler, Update update) {

    }

    private String generateHelpMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append("<b>/first_odbior [1,10]</b> - get list of first available dates. You can provide count argument").append(System.lineSeparator());
        builder.append("<b>/first_zlozenie [1,10]</b> - get list of first available dates. You can provide count argument").append(System.lineSeparator());
        builder.append("<b>/date_info yyyy-MM-dd</b> - get information about provided date. You have to provide date argument").append(System.lineSeparator());
        builder.append("<b>/link</b> - get Urzad URL").append(System.lineSeparator());
        builder.append("<b>/help</b> - list of available commands");
        return builder.toString();
    }

}
