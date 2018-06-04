package org.bot.commands.impl;

import org.bot.appointment.AppointmentDatesManager;
import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.bot.keyboards.MonthsKeyboard;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Calendar;
import java.util.Date;

@BotCommand(name = "cancel_appointment")
public class CancelAppointment extends Command {

    @Override
    public void process(CommandResultHandler handler, Update update) {

    }

    @Override
    public void processCallbackQuery(CommandResultHandler handler, Update update) {

    }

    private void showMonthsKeyboard(CommandResultHandler handler, Update update, boolean edit) {
        Calendar calendar = Calendar.getInstance();
        Date begin = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, AppointmentDatesManager.DAYS_TO_SCAN);
        Date end = calendar.getTime();
        System.out.println("showMonthsKeyboard in interval: [" + begin.toString() + "; " + end.toString() + "]");
        MonthsKeyboard builder = new MonthsKeyboard().begin(begin).end(end);
        InlineKeyboardMarkup keyboard = builder.create();
        if (edit) {
            System.out.println("Edit existing keyboard");
            EditMessageText message = new EditMessageText()
                    .enableHtml(true)
                    .setChatId(getChatId(update))
                    .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                    .setText("Select month")
                    .setReplyMarkup(keyboard);
            handler.execute(message);
        } else {
            System.out.println("Send new keyboard");
            SendMessage message = new SendMessage()
                    .enableHtml(true)
                    .setChatId(getChatId(update))
                    .setText("Select month")
                    .setReplyMarkup(keyboard);
            handler.execute(message);
        }
    }
}
