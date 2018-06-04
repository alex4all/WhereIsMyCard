package org.bot.commands.impl;

import org.bot.appointment.AppointmentDate;
import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.bot.keyboards.VerticalKeyboard;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;

@BotCommand(name = "first_appointment")
public class FirstAppointment extends Command {

    @Override
    public void process(CommandResultHandler handler, Update update) {
        VerticalKeyboard keyboard = new VerticalKeyboard()
                .elements(AppointmentDate.Type.values())
                .callbackPrefix("FirstAppointment");
        System.out.println("Send new keyboard");
        SendMessage message = new SendMessage()
                .enableHtml(true)
                .setChatId(getChatId(update))
                .setText("Select month")
                .setReplyMarkup(keyboard.create());
        handler.execute(message);
    }
}
