package org.bot.keyboards.adapter;

import org.bot.keyboards.VerticalKeyboard;
import org.bot.utils.DatesCompare;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class MonthKeyboardAdapter {

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

    public MonthKeyboardAdapter begin(Date begin) {
        this.begin = begin;
        return this;
    }

    public MonthKeyboardAdapter end(Date end) {
        this.end = end;
        return this;
    }

    public MonthKeyboardAdapter callbackPrefix(String callbackPrefix) {
        this.callbackPrefix = callbackPrefix;
        return this;
    }

    public MonthKeyboardAdapter datePattern(String datePattern) {
        this.datePattern = datePattern;
        return this;
    }

    public MonthKeyboardAdapter callbackDatePattern(String callBackDatePattern) {
        this.callBackDatePattern = callBackDatePattern;
        return this;
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

    public InlineKeyboardMarkup createKeyboard() {
        initialize();
        return keyboard.butid();
    }

    public boolean processCallback(String callback) {
        if (callback.startsWith(callbackPrefix)) {
            onMonthClick();
            return true;
        }
        return false;
    }

    public abstract void onMonthClick();
}
