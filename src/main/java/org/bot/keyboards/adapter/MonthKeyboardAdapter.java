package org.bot.keyboards.adapter;

import org.bot.commands.CommandResultHandler;
import org.bot.keyboards.VerticalKeyboard;
import org.bot.utils.DatesCompare;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class MonthKeyboardAdapter implements KeyboardAdapter {

    public static final String DEF_DATE_PATTERN = "LLLL YYYY";
    public static final String DEF_CALLBACK_DATE_PATTERN = "yyyy-MM-dd";
    public static final String MONTH_CLICK_PREFIX = "ClickOnMonth_";

    private String datePattern = DEF_DATE_PATTERN;
    private String callBackDatePattern = DEF_CALLBACK_DATE_PATTERN;
    private String callbackPrefix = MONTH_CLICK_PREFIX;
    private boolean initialized = false;
    private VerticalKeyboard keyboard = new VerticalKeyboard();

    private SimpleDateFormat textDateFormat;
    private SimpleDateFormat callbackFormat;
    private Map<String, String> keyboardData = new LinkedHashMap<>();

    private Date begin;
    private Date end;

    public MonthKeyboardAdapter(Date begin, Date end) {
        this.begin = begin;
        this.end = end;
    }

    private void initialize() {
        if (initialized)
            return;
        if (begin.getTime() > end.getTime())
            throw new RuntimeException("Begin time can't be more than end time");
        textDateFormat = new SimpleDateFormat(datePattern);
        callbackFormat = new SimpleDateFormat(callBackDatePattern);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(begin);
        System.out.println("Calendar begin: " + calendar.getTime().toString());
        System.out.println("Calendar end: " + end.toString());
        do {
            String textToDisplay = textDateFormat.format(calendar.getTime());
            String callback = callbackPrefix + callbackFormat.format(calendar.getTime());
            keyboardData.put(textToDisplay, callback);
            calendar.add(Calendar.MONTH, 1);
        } while (DatesCompare.beforeOrSameMonth(calendar.getTime(), end));
        keyboard.setElements(keyboardData);
        initialized = true;
        System.out.println("Months to display: " + keyboardData);
    }

    public void display(CommandResultHandler handler, Update update) {
        initialize();
        return keyboard.build();
    }

    public void display(CommandResultHandler handler, Update update, String text) {
        initialize();
        return keyboard.build();
    }

    @Override
    public boolean processCallback(CommandResultHandler handler, Update update) {
        String callbackQuery = update.getCallbackQuery().getData();
        if (callbackQuery.startsWith(callbackPrefix)) {
            String date = callbackQuery.substring(callbackPrefix.length());
            onMonthClick(date, handler, update);
            return true;
        }
        return false;
    }

    public abstract void onMonthClick(String date, CommandResultHandler handler, Update update);
}
