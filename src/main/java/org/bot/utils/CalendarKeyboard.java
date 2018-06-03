package org.bot.utils;

import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarKeyboard {

    public static final String DEF_HEADER_PATTERN = "LLLL YYYY";
    public static final String DEF_DAY_OF_WEEK_PATTERN = "EE";
    public static final String DEF_CALLBACK_DATE_PATTERN = "yyyy-MM-dd";
    public static final String DEF_CALLBACK_PREFIX = "ClickOnDay_";
    public static final String DEF_CALLBACK_PREVIOUS = "ClickOnPrevious";
    public static final String DEF_CALLBACK_BACK = "ClickOnBack";
    public static final String DEF_CALLBACK_NEXT = "ClickOnNext";

    private Date date;
    private String headerPattern = DEF_HEADER_PATTERN;
    private String dayOfWeekPattern = DEF_DAY_OF_WEEK_PATTERN;

    private String callbackDatePattern = DEF_CALLBACK_DATE_PATTERN;
    private String callbackPrefix = DEF_CALLBACK_PREFIX;
    private String callbackBack = DEF_CALLBACK_BACK;

    public CalendarKeyboard date(Date date) {
        this.date = date;
        System.out.println("CalendarKeyboard date: " + date.toString());
        return this;
    }

    public CalendarKeyboard headerPattern(String headerPattern) {
        this.headerPattern = headerPattern;
        return this;
    }

    public CalendarKeyboard dayOfWeekPattern(String dayOfWeekPattern) {
        this.dayOfWeekPattern = dayOfWeekPattern;
        return this;
    }

    public CalendarKeyboard callbackDatePattern(String callbackDatePattern) {
        this.callbackDatePattern = callbackDatePattern;
        return this;
    }


    public CalendarKeyboard callbackPrefix(String callbackPrefix) {
        this.callbackPrefix = callbackPrefix;
        return this;
    }

    public InlineKeyboardMarkup create() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // add date header: September 2018
        List<InlineKeyboardButton> header = new ArrayList<>(1);
        header.add(new InlineKeyboardButton().setText(new SimpleDateFormat(headerPattern).format(date)).setCallbackData("ignore"));
        rowsInline.add(header);

        // add list of week days
        String[] weekDays = getWeedDays();
        List<InlineKeyboardButton> daysOfWeek = new ArrayList<>(7);
        for (String dayOfWeed : weekDays) {
            daysOfWeek.add(new InlineKeyboardButton().setText(dayOfWeed).setCallbackData("ignore"));
        }
        rowsInline.add(daysOfWeek);

        // set calendar to begin of week
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        int currentMonth = calendar.get(Calendar.MONTH);

        // find first day of first week in this month
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.add(Calendar.DAY_OF_YEAR, 1 - calendar.get(Calendar.DAY_OF_WEEK));
        int firstWeekBegin = calendar.get(Calendar.DAY_OF_YEAR);
        System.out.println("firstWeekBegin: " + firstWeekBegin);

        // find last day of last week in this month
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.add(Calendar.DAY_OF_YEAR, 7 - calendar.get(Calendar.DAY_OF_WEEK));
        int lastWeekEnd = calendar.get(Calendar.DAY_OF_YEAR);
        System.out.println("lastWeekEnd: " + lastWeekEnd);

        SimpleDateFormat callbackFormat = new SimpleDateFormat(callbackDatePattern);
        calendar.set(Calendar.DAY_OF_YEAR, firstWeekBegin);
        do {
            daysOfWeek = new ArrayList<>(7);
            for (int i = 0; i < 7; i++) {
                if (calendar.get(Calendar.MONTH) == currentMonth) {
                    String dayOfMonth = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                    String callbackDate = callbackFormat.format(calendar.getTime());
                    daysOfWeek.add(new InlineKeyboardButton().setText(dayOfMonth).setCallbackData(callbackPrefix + callbackDate));
                } else {
                    daysOfWeek.add(new InlineKeyboardButton().setText(" ").setCallbackData("ignore"));
                }
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            rowsInline.add(daysOfWeek);
        } while (calendar.get(Calendar.DAY_OF_YEAR) <= lastWeekEnd);

        // back button
        List<InlineKeyboardButton> bottomPanel = new ArrayList<>(1);
        bottomPanel.add(new InlineKeyboardButton().setText("<").setCallbackData(DEF_CALLBACK_PREVIOUS));
        bottomPanel.add(new InlineKeyboardButton().setText("Back").setCallbackData(callbackBack));
        bottomPanel.add(new InlineKeyboardButton().setText(">").setCallbackData(DEF_CALLBACK_NEXT));
        rowsInline.add(bottomPanel);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    /**
     * Get add days of week in array
     */
    private String[] getWeedDays() {
        String[] days = new String[7];
        SimpleDateFormat dateFormat = new SimpleDateFormat(dayOfWeekPattern);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getMinimum(Calendar.DAY_OF_WEEK));
        days[0] = dateFormat.format(calendar.getTime());
        System.out.println("day of week: " + days[0]);
        for (int i = 1; i < 7; i++) {
            calendar.add(Calendar.DAY_OF_WEEK, 1);
            days[i] = dateFormat.format(calendar.getTime());
            System.out.println("day of week: " + days[i]);
        }
        return days;
    }
}
