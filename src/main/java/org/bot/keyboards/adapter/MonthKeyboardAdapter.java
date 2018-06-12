package org.bot.keyboards.adapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bot.commands.CommandResultHandler;
import org.bot.keyboards.Button;
import org.bot.keyboards.VerticalKeyboard;
import org.bot.utils.DatesCompare;
import org.bot.utils.MessageUtils;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public abstract class MonthKeyboardAdapter implements KeyboardAdapter {
    private static final Logger log = LogManager.getLogger(MonthKeyboardAdapter.class);
    private static final String DEF_DATE_PATTERN = "LLLL YYYY";
    private static final String DEF_CALLBACK_DATE_PATTERN = "yyyy-MM-dd";
    private static final String MONTH_CLICK_PREFIX = "ClickOnMonth_";
    private static final String DEF_HEADER = "Select day of month";

    private SimpleDateFormat callbackFormat = new SimpleDateFormat(DEF_CALLBACK_DATE_PATTERN);

    private boolean initialized = false;
    private VerticalKeyboard keyboard = new VerticalKeyboard();

    private List<Button> keyboardData = new ArrayList<>();

    private Date begin;
    private Date end;

    /**
     * Keep message with keyboard to be able to edit it
     */
    private Message keyboardMessage;

    public MonthKeyboardAdapter(Date begin, Date end) {
        this.begin = begin;
        this.end = end;
    }

    private void initialize() {
        if (initialized)
            return;
        if (begin.getTime() > end.getTime())
            throw new RuntimeException("Begin time can't be more than end time");
        SimpleDateFormat textDateFormat = new SimpleDateFormat(DEF_DATE_PATTERN);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(begin);
        log.info("Calendar begin: " + calendar.getTime().toString());
        log.info("Calendar end: " + end.toString());
        do {
            String textToDisplay = textDateFormat.format(calendar.getTime());
            String callback = MONTH_CLICK_PREFIX + callbackFormat.format(calendar.getTime());
            keyboardData.add(new Button(textToDisplay, callback));
            calendar.add(Calendar.MONTH, 1);
        } while (DatesCompare.beforeOrSameMonth(calendar.getTime(), end));
        keyboard.setButtons(keyboardData);
        initialized = true;
    }

    public void display(CommandResultHandler handler, Long chatId) {
        // some sort of caching
        initialize();
        log.info("Months to display: " + keyboardData);
        keyboardMessage = MessageUtils.sendOrEdit(keyboardMessage, chatId, DEF_HEADER, keyboard.build(), handler);
    }

    @Override
    public boolean processCallback(CommandResultHandler handler, Update update) {
        String callbackQuery = update.getCallbackQuery().getData();
        try {
            if (callbackQuery.startsWith(MONTH_CLICK_PREFIX)) {
                String date = callbackQuery.substring(MONTH_CLICK_PREFIX.length());
                onMonthClick(callbackFormat.parse(date), handler, update);
                return true;
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public abstract void onMonthClick(Date date, CommandResultHandler handler, Update update);

    public void setKeyboardMessage(Message keyboardMessage) {
        this.keyboardMessage = keyboardMessage;
    }

    public Message getKeyboardMessage() {
        return keyboardMessage;
    }
}
