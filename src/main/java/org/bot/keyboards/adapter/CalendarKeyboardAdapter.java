package org.bot.keyboards.adapter;

import org.bot.commands.CommandResultHandler;
import org.bot.keyboards.Button;
import org.bot.keyboards.GridKeyboard;
import org.bot.utils.DatesCompare;
import org.bot.utils.MessageUtils;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;

import java.text.SimpleDateFormat;
import java.util.*;

public abstract class CalendarKeyboardAdapter implements KeyboardAdapter {
    private static final String DEF_HEADER_PATTERN = "LLLL YYYY";
    private static final String DEF_DAY_OF_WEEK_PATTERN = "EE";
    private static final String DEF_CALLBACK_DATE_PATTERN = "yyyy-MM-dd";
    private static final String DEF_HEADER = "Select day";

    protected enum Event {CLICK_NEXT, CLICK_PREV, CLICK_DAY, CLICK_BACK}

    protected SimpleDateFormat headerFormat = new SimpleDateFormat(DEF_HEADER_PATTERN);
    protected SimpleDateFormat callbackFormat = new SimpleDateFormat(DEF_CALLBACK_DATE_PATTERN);

    private Date monthToDisplay;
    private Date begin;
    private Date end;
    private String[] daysOfWeek;
    private GridKeyboard keyboard;
    private Message keyboardMessage;

    public CalendarKeyboardAdapter(Date begin, Date end) {
        this.begin = begin;
        this.end = end;
        keyboard = new GridKeyboard();
    }

    @Override
    public boolean processCallback(CommandResultHandler handler, Update update) {
        String callbackQuery = update.getCallbackQuery().getData();
        try {
            if (callbackQuery.startsWith(Event.CLICK_NEXT.name())) {
                String nextDate = callbackQuery.substring(Event.CLICK_NEXT.name().length());
                onNextDayClick(callbackFormat.parse(nextDate), handler, update);
                return true;
            }
            if (callbackQuery.startsWith(Event.CLICK_PREV.name())) {
                String previousDate = callbackQuery.substring(Event.CLICK_PREV.name().length());
                onPreviousDayClick(callbackFormat.parse(previousDate), handler, update);
                return true;
            }
            if (callbackQuery.startsWith(Event.CLICK_DAY.name())) {
                String date = callbackQuery.substring(Event.CLICK_DAY.name().length());
                onDayClick(callbackFormat.parse(date), handler, update);
                return true;
            }
            if (callbackQuery.equals(Event.CLICK_BACK.name())) {
                onBackClick(handler, update);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return false;
    }

    private void onNextDayClick(Date nextDate, CommandResultHandler handler, Update update) {
        display(nextDate, handler, update.getCallbackQuery().getMessage().getChatId());
    }

    private void onPreviousDayClick(Date previousDate, CommandResultHandler handler, Update update) {
        display(previousDate, handler, update.getCallbackQuery().getMessage().getChatId());
    }

    public abstract void onDayClick(Date date, CommandResultHandler handler, Update update);

    public abstract void onBackClick(CommandResultHandler handler, Update update);

    public void display(Date date, CommandResultHandler handler, Long chatId) {
        monthToDisplay = date;
        keyboard.clear();
        keyboard.addRow(createHeader());
        keyboard.addRow(createDaysOfWeekNames());
        keyboard.addGrid(createWeedDaysGrid());
        keyboard.addRow(createBottomPanel());
        keyboardMessage = MessageUtils.sendOrEdit(keyboardMessage, chatId, DEF_HEADER, keyboard.build(), handler);
    }

    private List<Button> createHeader() {
        List<Button> header = new ArrayList<>(1);
        String monthHeader = headerFormat.format(monthToDisplay);
        header.add(new Button(monthHeader, "ignore"));
        return header;
    }

    private List<Button> createDaysOfWeekNames() {
        List<Button> daysOfWeekNames = new ArrayList<>(7);
        String[] daysOfWeek = getDaysOfWeek();
        for (String dayOfWeed : daysOfWeek) {
            daysOfWeekNames.add(new Button(dayOfWeed, "ignore"));
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
        System.out.println("firstWeekBegin: " + firstWeekBegin.toString());

        // find last day of last week of this month (last day of week possible can be in next month)
        calendar.setTime(monthToDisplay);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.add(Calendar.DAY_OF_YEAR, 7 - calendar.get(Calendar.DAY_OF_WEEK));
        Date lastDayOfLastWeek = calendar.getTime();
        System.out.println("lastDayOfLastWeek: " + lastDayOfLastWeek.toString());

        // go through all days starting from first day of first week and up to the last day of last week
        calendar.setTime(firstWeekBegin);
        do {
            // Add days to grid week by week
            List<Button> daysOfWeek = new ArrayList<>(7);
            for (int i = 0; i < 7; i++) {
                if (calendar.get(Calendar.MONTH) == monthId) {
                    daysOfWeek.add(new Button(getDayText(calendar), getDayCallback(calendar)));
                } else {
                    daysOfWeek.add(new Button(" ", "ignore"));
                }
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            weedDaysGreed.add(daysOfWeek);
        } while (DatesCompare.beforeOrSameDay(calendar.getTime(), lastDayOfLastWeek));
        return weedDaysGreed;
    }

    /**
     * Allows to override default behavior and implement different data display strategies
     *
     * @param calendar
     * @return
     */
    protected String getDayText(Calendar calendar) {
        String dayOfMonth = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        return dayOfMonth;
    }

    /**
     * Allows to override default behavior and implement different data display strategies
     *
     * @param calendar
     * @return
     */
    protected String getDayCallback(Calendar calendar) {
        String callbackDate = callbackFormat.format(calendar.getTime());
        return Event.CLICK_DAY + callbackDate;
    }

    private List<Button> createBottomPanel() {
        List<Button> bottomPanel = new ArrayList<>(3);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(monthToDisplay);
        calendar.add(Calendar.MONTH, -1);
        // clear left button if prev month if out of range
        if (begin == null) {
            String callbackDate = callbackFormat.format(calendar.getTime());
            bottomPanel.add(new Button("<", Event.CLICK_PREV + callbackDate));
        } else {
            if (DatesCompare.afterOrSameMonth(calendar.getTime(), begin)) {
                String callbackDate = callbackFormat.format(calendar.getTime());
                bottomPanel.add(new Button("<", Event.CLICK_PREV + callbackDate));
            } else {
                bottomPanel.add(new Button(" ", "ignore"));
            }
        }

        bottomPanel.add(new Button("Back", Event.CLICK_BACK.toString()));

        calendar.setTime(monthToDisplay);
        calendar.add(Calendar.MONTH, 1);
        // clear right button if next month if out of range
        if (end == null) {
            String callbackDate = callbackFormat.format(calendar.getTime());
            bottomPanel.add(new Button(">", Event.CLICK_NEXT + callbackDate));
        } else {
            if (DatesCompare.beforeOrSameMonth(calendar.getTime(), end)) {
                String callbackDate = callbackFormat.format(calendar.getTime());
                bottomPanel.add(new Button(">", Event.CLICK_NEXT + callbackDate));
            } else {
                bottomPanel.add(new Button(" ", "ignore"));
            }
        }
        return bottomPanel;
    }

    /**
     * Get add days of week in array
     */
    private String[] getDaysOfWeek() {
        if (this.daysOfWeek != null)
            return this.daysOfWeek;
        String[] daysOfWeek = new String[7];
        SimpleDateFormat dateFormat = new SimpleDateFormat(DEF_DAY_OF_WEEK_PATTERN);
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

    public void setKeyboardMessage(Message keyboardMessage) {
        this.keyboardMessage = keyboardMessage;
    }

    public Message getKeyboardMessage() {
        return keyboardMessage;
    }
}
