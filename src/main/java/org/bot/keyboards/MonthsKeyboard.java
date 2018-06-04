package org.bot.keyboards;

import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class MonthsKeyboard {

    public static final String DEF_DATE_PATTERN = "LLLL YYYY";
    public static final String DEF_CALLBACK_DATE_PATTERN = "yyyy-MM-dd";
    public static final String MONTH_CLICK_PREFIX = "ClickOnMonth_";

    private Date begin;
    private Date end;

    private String datePattern = DEF_DATE_PATTERN;
    private String callBackDatePattern = DEF_CALLBACK_DATE_PATTERN;
    private String callbackPrefix = MONTH_CLICK_PREFIX;

    public MonthsKeyboard begin(Date begin) {
        this.begin = begin;
        return this;
    }

    public MonthsKeyboard end(Date end) {
        this.end = end;
        return this;
    }

    public MonthsKeyboard callbackPrefix(String callbackPrefix) {
        this.callbackPrefix = callbackPrefix;
        return this;
    }

    public MonthsKeyboard datePattern(String datePattern) {
        this.datePattern = datePattern;
        return this;
    }

    public MonthsKeyboard callbackDatePattern(String callBackDatePattern) {
        this.callBackDatePattern = callBackDatePattern;
        return this;
    }

    public InlineKeyboardMarkup create() {
        if (begin.getTime() > end.getTime())
            throw new RuntimeException("Begin time can't be more than end time");
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();


        Calendar calendar = Calendar.getInstance();
        calendar.setTime(begin);
        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTime(end);
        SimpleDateFormat textDateFormat = new SimpleDateFormat(datePattern);
        SimpleDateFormat callbackFormat = new SimpleDateFormat(callBackDatePattern);

        LocalDate endDate = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int endMonth = endDate.getMonth().getValue();
        int endYear = endDate.getYear();
        int currentMonth;
        int currentYear;
        boolean sameYear;
        boolean diffYear;
        do {
            String textDate = textDateFormat.format(calendar.getTime());
            String callbackDate = callbackFormat.format(calendar.getTime());
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(new InlineKeyboardButton().setText(textDate).setCallbackData(callbackPrefix + callbackDate));
            rowsInline.add(rowInline);
            calendar.add(Calendar.MONTH, 1);
            LocalDate currentDate = calendar.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            currentMonth = currentDate.getMonth().getValue();
            currentYear = currentDate.getYear();
            diffYear = currentYear < endYear;
            sameYear = currentYear == endYear & currentMonth <= endMonth;
        } while (diffYear || sameYear);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}
