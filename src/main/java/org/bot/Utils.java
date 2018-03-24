package org.bot;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class Utils {
    public static long dateToMills(String date, DateFormat format) throws ParseException {
        return format.parse(date).getTime();
    }

    public static String millsToDate(long time, DateFormat format) {
        return format.format(new Date(time));
    }
}
