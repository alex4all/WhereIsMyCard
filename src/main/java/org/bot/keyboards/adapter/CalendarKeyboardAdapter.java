package org.bot.keyboards.adapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bot.commands.Command;
import org.bot.commands.Context;
import org.bot.keyboards.Button;
import org.bot.keyboards.GridKeyboard;
import org.bot.utils.DatesCompare;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Update;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class CalendarKeyboardAdapter implements KeyboardAdapter {
    private static final Logger log = LogManager.getLogger(CalendarKeyboardAdapter.class);
    private static final String DEF_HEADER_PATTERN = "LLLL YYYY";
    private static final String DEF_DAY_OF_WEEK_PATTERN = "EE";
    private static final String DEF_CALLBACK_DATE_PATTERN = "yyyy-MM-dd";

    protected enum Event {CLICK_NEXT, CLICK_PREV, CLICK_DAY, CLICK_BACK, CLICK_OUT_OF_RANGE}

    protected SimpleDateFormat callbackFormat = new SimpleDateFormat(DEF_CALLBACK_DATE_PATTERN);

    private Date monthToDisplay;
    private Date begin;
    private Date end;
    private String[] daysOfWeek;
    private GridKeyboard keyboard;

    public CalendarKeyboardAdapter(Date begin, Date end) {
        this.begin = begin;
        this.end = end;
        keyboard = new GridKeyboard();
    }

    @Override
    public boolean processCallback(Context context, Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String queryText = callbackQuery.getData();
        try {
            if (queryText.startsWith(Event.CLICK_NEXT.name())) {
                String nextDate = queryText.substring(Event.CLICK_NEXT.name().length());
                onNextDayClick(callbackFormat.parse(nextDate), context, callbackQuery);
                return true;
            }
            if (queryText.startsWith(Event.CLICK_PREV.name())) {
                String previousDate = queryText.substring(Event.CLICK_PREV.name().length());
                onPreviousDayClick(callbackFormat.parse(previousDate), context, callbackQuery);
                return true;
            }
            if (queryText.startsWith(Event.CLICK_DAY.name())) {
                String date = queryText.substring(Event.CLICK_DAY.name().length());
                onDayClick(callbackFormat.parse(date), context, callbackQuery);
                return true;
            }
            if (queryText.equals(Event.CLICK_BACK.name())) {
                onBackClick(context, callbackQuery);
                return true;
            }
            if (queryText.startsWith(Event.CLICK_OUT_OF_RANGE.name())) {
                String date = queryText.substring(Event.CLICK_OUT_OF_RANGE.name().length());
                onOutOfRange(callbackFormat.parse(date), context, callbackQuery);
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private void onNextDayClick(Date nextDate, Context context, CallbackQuery query) {
        display(nextDate, context);
        context.ignoreCallback(query);
    }

    private void onPreviousDayClick(Date previousDate, Context context, CallbackQuery query) {
        display(previousDate, context);
        context.ignoreCallback(query);
    }

    private void onOutOfRange(Date date, Context context, CallbackQuery callbackQuery) {
        SimpleDateFormat headerFormat = new SimpleDateFormat(DEF_HEADER_PATTERN, context.getLocale());
        String message = headerFormat.format(date) + " " + context.getResource("keyboard.calendar.outOfRange");
        context.answerCallbackQuery(message, callbackQuery);
    }

    public abstract void onDayClick(Date date, Context context, CallbackQuery query);

    public abstract void onBackClick(Context context, CallbackQuery query);

    public void display(Date date, Context context) {
        monthToDisplay = date;
        keyboard.clear();
        keyboard.addRow(createHeader(context.getLocale()));
        keyboard.addRow(createDaysOfWeekNames(context.getLocale()));
        keyboard.addGrid(createWeedDaysGrid());
        keyboard.addRow(createBottomPanel(context));
        context.showKeyboard(context.getResource("keyboard.calendar.selectDay"), keyboard.build());
    }

    private List<Button> createHeader(Locale locale) {
        List<Button> header = new ArrayList<>(1);
        SimpleDateFormat headerFormat = new SimpleDateFormat(DEF_HEADER_PATTERN, locale);
        String monthHeader = headerFormat.format(monthToDisplay);
        header.add(new Button(monthHeader, Command.IGNORE_QUERY));
        return header;
    }

    private List<Button> createDaysOfWeekNames(Locale locale) {
        List<Button> daysOfWeekNames = new ArrayList<>(7);
        String[] daysOfWeek = getDaysOfWeek(locale);
        for (String dayOfWeed : daysOfWeek) {
            daysOfWeekNames.add(new Button(dayOfWeed, Command.IGNORE_QUERY));
        }
        return daysOfWeekNames;
    }

    private List<List<Button>> createWeedDaysGrid() {
        List<List<Button>> weedDaysGreed = new LinkedList<>();
        // set calendar first day of month
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(monthToDisplay);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        int monthId = calendar.get(Calendar.MONTH);

        // find first day of first week in this month (first day of week possible can be in prev month)
        calendar.add(Calendar.DAY_OF_YEAR, 1 - calendar.get(Calendar.DAY_OF_WEEK));
        Date firstWeekBegin = calendar.getTime();
        log.info("firstWeekBegin: " + firstWeekBegin.toString());

        // find last day of last week of this month (last day of week possible can be in next month)
        calendar.setTime(monthToDisplay);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.add(Calendar.DAY_OF_YEAR, 7 - calendar.get(Calendar.DAY_OF_WEEK));
        Date lastDayOfLastWeek = calendar.getTime();
        log.info("lastDayOfLastWeek: " + lastDayOfLastWeek.toString());

        // go through all days starting from first day of first week and up to the last day of last week
        calendar.setTime(firstWeekBegin);
        do {
            // Add days to grid week by week
            List<Button> daysOfWeek = new ArrayList<>(7);
            for (int i = 0; i < 7; i++) {
                if (calendar.get(Calendar.MONTH) == monthId) {
                    daysOfWeek.add(new Button(getDayText(calendar), getDayCallback(calendar)));
                } else {
                    daysOfWeek.add(new Button(" ", Command.IGNORE_QUERY));
                }
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            weedDaysGreed.add(daysOfWeek);
        } while (DatesCompare.beforeOrSameDay(calendar.getTime(), lastDayOfLastWeek));
        return weedDaysGreed;
    }

    /**
     * Allows to override default behavior and implement different data display strategies
     */
    protected String getDayText(Calendar calendar) {
        return String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Allows to override default behavior and implement different data display strategies
     */
    protected String getDayCallback(Calendar calendar) {
        String callbackDate = callbackFormat.format(calendar.getTime());
        return Event.CLICK_DAY + callbackDate;
    }

    private List<Button> createBottomPanel(Context context) {
        List<Button> bottomPanel = new ArrayList<>(3);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(monthToDisplay);
        // create callback for prev month button
        calendar.add(Calendar.MONTH, -1);
        String callbackDate = callbackFormat.format(calendar.getTime());
        String prevMonthCallback;
        if (begin == null) {
            prevMonthCallback = Event.CLICK_PREV + callbackDate;
        } else {
            if (DatesCompare.afterOrSameMonth(calendar.getTime(), begin)) {
                prevMonthCallback = Event.CLICK_PREV + callbackDate;
            } else {
                prevMonthCallback = Event.CLICK_OUT_OF_RANGE + callbackDate;
            }
        }

        calendar.setTime(monthToDisplay);
        // create callback for next month button
        calendar.add(Calendar.MONTH, 1);
        callbackDate = callbackFormat.format(calendar.getTime());
        String nextMonthCallback;
        if (end == null) {
            nextMonthCallback = Event.CLICK_NEXT + callbackDate;
        } else {
            if (DatesCompare.beforeOrSameMonth(calendar.getTime(), end)) {
                nextMonthCallback = Event.CLICK_NEXT + callbackDate;
            } else {
                nextMonthCallback = Event.CLICK_OUT_OF_RANGE + callbackDate;
            }
        }

        bottomPanel.add(new Button("<", prevMonthCallback));
        bottomPanel.add(new Button(context.getResource("keyboard.calendar.back"), Event.CLICK_BACK.toString()));
        bottomPanel.add(new Button(">", nextMonthCallback));
        return bottomPanel;
    }

    /**
     * Get add days of week in array
     */
    private String[] getDaysOfWeek(Locale locale) {
        if (this.daysOfWeek != null)
            return this.daysOfWeek;
        String[] daysOfWeek = new String[7];
        SimpleDateFormat dateFormat = new SimpleDateFormat(DEF_DAY_OF_WEEK_PATTERN, locale);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getMinimum(Calendar.DAY_OF_WEEK));
        daysOfWeek[0] = dateFormat.format(calendar.getTime());
        for (int i = 1; i < 7; i++) {
            calendar.add(Calendar.DAY_OF_WEEK, 1);
            daysOfWeek[i] = dateFormat.format(calendar.getTime());
        }
        this.daysOfWeek = daysOfWeek;
        return daysOfWeek;
    }
}
