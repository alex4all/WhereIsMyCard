package org.bot.commands.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bot.appointment.AppointmentDate;
import org.bot.appointment.AppointmentsManager;
import org.bot.commands.BotCommand;
import org.bot.commands.Command;
import org.bot.commands.CommandResultHandler;
import org.bot.commands.Context;
import org.bot.keyboards.Button;
import org.bot.keyboards.HorizontalKeyboard;
import org.bot.keyboards.adapter.AppointmentTimeAdapter;
import org.bot.keyboards.adapter.CalendarKeyboardAdapter;
import org.bot.keyboards.adapter.ExcludingCalendarAdapter;
import org.bot.utils.CommandsHistory;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@BotCommand(name = "make_appointment")
public class MakeAppointment extends Command {
    private static final Logger log = LogManager.getLogger(MakeAppointment.class);
    private static final AppointmentsManager DATES_MANAGER = AppointmentsManager.getInstance();

    private AppointmentDate.Type type;
    private String date;
    private String time;
    private String email;
    private String userName;

    public MakeAppointment(CommandResultHandler handler, Update update) {
        super(handler, update);
    }

    @Override
    public void process(Update update) {
        showAppointmentTypes(null);
    }

    @Override
    public void processCallbackQuery(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String query = callbackQuery.getData();
        query = query.replace("/make_appointment", "");

        if (CalendarKeyboardAdapter.isCallbackHandler(query)) {
            DateInfoCalendarAdapter calendarAdapter = new DateInfoCalendarAdapter();
            calendarAdapter.processCallback(this, update);
            return;
        }

        if (AppointmentTimeAdapter.isCallbackHandler(query)) {

        }
        if (calendarAdapter.processCallback(this, update))
            return;
        query.star
        AppointmentDate.Type type = AppointmentDate.Type.valueOf(query);

    }

    private void showAppointmentTypes(CallbackQuery query) {
        List<Button> buttons = new ArrayList<>(AppointmentDate.Type.values().length);
        for (AppointmentDate.Type type : AppointmentDate.Type.values()) {
            buttons.add(new Button(type.name(), type.name()));
        }
        HorizontalKeyboard keyboard = new HorizontalKeyboard();
        keyboard.setButtons(buttons);
        query.getMessage().getMessageId();
        String keyboardHeader = getResource("command.firstAppointment.selectType");
        if (query != null)
            editKeyboard(keyboardHeader, keyboard.create(), query.getMessage().getMessageId());
        else
            showKeyboard(keyboardHeader, keyboard.create());
    }

    private void showAppointmentTime(Date date, CallbackQuery query) {

        //MakeAppointmentAdapter adapter = new MakeAppointmentAdapter(this, getName());
    }

    private void showAwaitEmailInput(AppointmentDate.Type type, String date, String time, CallbackQuery callbackQuery) {

    }

    @Override
    public String getName() {
        return "make_appointment";
    }

    private class DateInfoCalendarAdapter extends ExcludingCalendarAdapter {
        @Override
        public void onDayClick(Date date, Context context, CallbackQuery query) {
            showAppointmentTime(date, query);
            context.ignoreCallback(query);
        }

        @Override
        public void onBackClick(Context context, CallbackQuery query) {
            showAppointmentTypes(query);
            context.ignoreCallback(query);
        }
    }

    private class MakeAppointmentAdapter extends AppointmentTimeAdapter {
        public MakeAppointmentAdapter(AppointmentDate appointment, String callbackCommand) {
            super(appointment, callbackCommand);
        }

        @Override
        public void onTimeClick(AppointmentDate.Type type, String date, String time, CallbackQuery callbackQuery) {
            showAwaitEmailInput(type, date, time, callbackQuery);
            ignoreCallback(callbackQuery);
        }


    }
}
