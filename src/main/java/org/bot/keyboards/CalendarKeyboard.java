package org.bot.keyboards;

import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarKeyboard {

    public static final String DEF_HEADER_PATTERN = "LLLL YYYY";
    public static final String DEF_DAY_OF_WEEK_PATTERN = "EE";
    public static final String DEF_CALLBACK_DATE_PATTERN = "yyyy-MM-dd";

    public static final String DAY_CLICK_PREFIX = "ClickOnDay_";
    public static final String PREVIOUS_CLICK = "ClickOnPrevious_";
    public static final String NEXT_CLICK_PREFIX = "ClickOnNext_";
    public static final String BACK_CLICK_PREFIX = "ClickOnBack";
    public static final String WARNING_PREFIX = "WARNING";

    private Date month;
    private Date begin;
    private Date end;
    private String headerPattern = DEF_HEADER_PATTERN;
    private String dayOfWeekPattern = DEF_DAY_OF_WEEK_PATTERN;

    private String callbackDatePattern = DEF_CALLBACK_DATE_PATTERN;
    private String callbackPrefix = DAY_CLICK_PREFIX;
    private String callbackBack = BACK_CLICK_PREFIX;

    public CalendarKeyboard month(Date month) {
        this.month = month;
        return this;
    }

    public CalendarKeyboard begin(Date begin) {
        this.begin = begin;
        return this;
    }

    public CalendarKeyboard end(Date end) {
        this.end = end;
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
        rowsInline.add(createHeader());
        rowsInline.add(createDaysOfWeekNames());
        rowsInline.addAll(createWeedDaysGreed());
        rowsInline.add(createBottomPanel());
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    private List<InlineKeyboardButton> createHeader() {
        List<InlineKeyboardButton> header = new ArrayList<>(1);
        header.add(new InlineKeyboardButton().setText(new SimpleDateFormat(headerPattern).format(month)).setCallbackData("ignore"));
        return header;
    }

    private List<InlineKeyboardButton> createDaysOfWeekNames() {
        String[] weekDays = getWeedDays();
        List<InlineKeyboardButton> daysOfWeek = new ArrayList<>(7);
        for (String dayOfWeed : weekDays) {
            daysOfWeek.add(new InlineKeyboardButton().setText(dayOfWeed).setCallbackData("ignore"));
        }
        return daysOfWeek;
    }

    private List<List<InlineKeyboardButton>> createWeedDaysGreed() {
        List<List<InlineKeyboardButton>> weedDaysGreed = new ArrayList<>();
        // set calendar to begin of week
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(month);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        int currentMonth = calendar.get(Calendar.MONTH);

        // find first day of first week in this month
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.add(Calendar.DAY_OF_YEAR, 1 - calendar.get(Calendar.DAY_OF_WEEK));
        int firstWeekBegin = calendar.get(Calendar.DAY_OF_YEAR);
        System.out.println("firstWeekBegin: " + firstWeekBegin);

        // find last day of last week in this month
        calendar.setTime(month);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.add(Calendar.DAY_OF_YEAR, 7 - calendar.get(Calendar.DAY_OF_WEEK));
        int lastWeekEnd = calendar.get(Calendar.DAY_OF_YEAR);
        System.out.println("lastWeekEnd: " + lastWeekEnd);

        SimpleDateFormat callbackFormat = new SimpleDateFormat(callbackDatePattern);
        calendar.set(Calendar.DAY_OF_YEAR, firstWeekBegin);
        do {
            List<InlineKeyboardButton> daysOfWeek = new ArrayList<>(7);
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
            weedDaysGreed.add(daysOfWeek);
        } while (calendar.get(Calendar.DAY_OF_YEAR) <= lastWeekEnd);
        return weedDaysGreed;
    }

    private List<InlineKeyboardButton> createBottomPanel() {
        List<InlineKeyboardButton> bottomPanel = new ArrayList<>(1);
        InlineKeyboardButton prevButton = new InlineKeyboardButton().setText("<").setCallbackData("ignore");
        InlineKeyboardButton backButton = new InlineKeyboardButton().setText("Back").setCallbackData(callbackBack);
        InlineKeyboardButton nextButton = new InlineKeyboardButton().setText(">").setCallbackData("ignore");
        bottomPanel.add(prevButton);
        bottomPanel.add(backButton);
        bottomPanel.add(nextButton);

        SimpleDateFormat callbackFormat = new SimpleDateFormat(callbackDatePattern);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(month);
        calendar.add(Calendar.MONTH, -1);
        if (begin == null) {
            String callbackDate = callbackFormat.format(calendar.getTime());
            prevButton.setCallbackData(PREVIOUS_CLICK + callbackDate);
        } else {
            Date prevDate = calendar.getTime();
            int prevMonth = prevDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonth().getValue();
            int minBorder = begin.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonth().getValue();
            System.out.println("prevMonth: " + prevMonth);
            System.out.println("minBorder: " + minBorder);
            if (prevMonth >= minBorder) {
                String callbackDate = callbackFormat.format(calendar.getTime());
                prevButton.setCallbackData(PREVIOUS_CLICK + callbackDate);
            } else {
                prevButton.setCallbackData(WARNING_PREFIX + "Previous month is out of range");
            }
        }

        calendar.setTime(month);
        calendar.add(Calendar.MONTH, 1);
        if (end == null) {
            String callbackDate = callbackFormat.format(calendar.getTime());
            nextButton.setCallbackData(NEXT_CLICK_PREFIX + callbackDate);
        } else {
            Date nextDate = calendar.getTime();
            int nextMonth = nextDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonth().getValue();
            int maxBorder = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonth().getValue();
            System.out.println("nextMonth: " + nextMonth);
            System.out.println("maxBorder: " + maxBorder);
            if (nextMonth <= maxBorder) {
                String callbackDate = callbackFormat.format(calendar.getTime());
                nextButton.setCallbackData(NEXT_CLICK_PREFIX + callbackDate);
            } else {
                nextButton.setCallbackData(WARNING_PREFIX + "Next month is out of range");
            }
        }
        return bottomPanel;
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
        for (int i = 1; i < 7; i++) {
            calendar.add(Calendar.DAY_OF_WEEK, 1);
            days[i] = dateFormat.format(calendar.getTime());
        }
        return days;
    }
}