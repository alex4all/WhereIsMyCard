package org.bot.keyboards.adapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bot.commands.Context;
import org.bot.keyboards.Button;
import org.bot.keyboards.VerticalKeyboard;
import org.bot.utils.DatesCompare;
import org.telegram.telegrambots.api.objects.Update;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class MonthKeyboardAdapter implements KeyboardAdapter {
    private static final Logger log = LogManager.getLogger(MonthKeyboardAdapter.class);
    private static final String DEF_DATE_PATTERN = "LLLL YYYY";
    private static final String DEF_CALLBACK_DATE_PATTERN = "yyyy-MM-dd";
    private static final String MONTH_CLICK_PREFIX = "ClickOnMonth_";

    private SimpleDateFormat callbackFormat = new SimpleDateFormat(DEF_CALLBACK_DATE_PATTERN);

    private boolean initialized = false;
    private VerticalKeyboard keyboard = new VerticalKeyboard();

    private List<Button> keyboardData = new ArrayList<>();

    private Date begin;
    private Date end;

    public MonthKeyboardAdapter(Date begin, Date end) {
        this.begin = begin;
        this.end = end;
    }

    private void initialize(Locale locale) {
        if (initialized)
            return;
        if (begin.getTime() > end.getTime())
            throw new RuntimeException("Begin time can't be more than end time");
        SimpleDateFormat textDateFormat = new SimpleDateFormat(DEF_DATE_PATTERN, locale);
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

    public void display(Context context) {
        // some sort of caching
        initialize(context.getLocale());
        log.info("Months to display: " + keyboardData);
        context.showKeyboard(context.getResource("keyboard.months.selectMonth"), keyboard.build());
    }

    @Override
    public boolean processCallback(Context context, Update update) {
        String callbackQuery = update.getCallbackQuery().getData();
        try {
            if (callbackQuery.startsWith(MONTH_CLICK_PREFIX)) {
                String date = callbackQuery.substring(MONTH_CLICK_PREFIX.length());
                onMonthClick(callbackFormat.parse(date), context, update);
                return true;
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public abstract void onMonthClick(Date date, Context context, Update update);
}
