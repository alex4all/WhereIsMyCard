package org.bot.keyboards.adapter;

import org.bot.commands.CommandResultHandler;
import org.bot.keyboards.CalendarKeyboard;
import org.telegram.telegrambots.api.objects.Update;

import java.util.Date;

public abstract class CalendarKeyboardAdapter implements KeyboardAdapter {

    public static final String DEF_HEADER_PATTERN = "LLLL YYYY";
    public static final String DEF_DAY_OF_WEEK_PATTERN = "EE";
    public static final String DEF_CALLBACK_DATE_PATTERN = "yyyy-MM-dd";

    public static final String DAY_CLICK_PREFIX = "ClickOnDay_";
    public static final String PREVIOUS_CLICK = "ClickOnPrevious_";
    public static final String NEXT_CLICK_PREFIX = "ClickOnNext_";
    public static final String BACK_CLICK_PREFIX = "ClickOnBack";
    public static final String WARNING_PREFIX = "WARNING";

    private Date currentMonth;
    private Date begin;
    private Date end;
    private String headerPattern = DEF_HEADER_PATTERN;
    private String dayOfWeekPattern = DEF_DAY_OF_WEEK_PATTERN;

    private String callbackDatePattern = DEF_CALLBACK_DATE_PATTERN;
    private String callbackPrefix = DAY_CLICK_PREFIX;
    private String callbackBack = BACK_CLICK_PREFIX;

    public CalendarKeyboardAdapter() {
        this.begin = begin;
        this.end = end;

    }

    @Override
    public boolean processCallback(CommandResultHandler handler, Update update) {
        String callbackQuery = update.getCallbackQuery().getData();
        if (callbackQuery.startsWith(CalendarKeyboard.NEXT_CLICK_PREFIX)) {
            String date = callbackQuery.substring(CalendarKeyboard.NEXT_CLICK_PREFIX.length());
            showCalendarKeyboard(date, handler, update);
        }
        else if (callbackQuery.startsWith(CalendarKeyboard.PREVIOUS_CLICK)) {
            String date = callbackQuery.substring(CalendarKeyboard.PREVIOUS_CLICK.length());
            showCalendarKeyboard(date, handler, update);
        } else else if (callbackQuery.startsWith(CalendarKeyboard.DAY_CLICK_PREFIX)) {
            showDayInfo(handler, update);
        } else if (callbackQuery.equals(CalendarKeyboard.BACK_CLICK_PREFIX)) {
            showMonthsKeyboard(handler, update, true);
        }
        return false;
    }

    public abstract void onNextDayClick(String nextDate, CommandResultHandler handler, Update update);

    public abstract void onPreviousDayClick(String previousDate, CommandResultHandler handler, Update update);

    public abstract void onDayClick(String date, CommandResultHandler handler, Update update);

    public abstract void onBackClick(CommandResultHandler handler, Update update);
}
