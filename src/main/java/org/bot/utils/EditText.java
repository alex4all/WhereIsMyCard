package org.bot.utils;

import org.bot.commands.Context;

public class EditText {
    public static String bold(String text) {
        return "<b>" + text + "</b>";
    }

    public static String italic(String text) {
        return "<i>" + text + "</i>";
    }

    public static String timeAfterUpdate(long minutesAgo, Context context) {
        String template;
        long lastMinute = minutesAgo % 10;
        // if updated 1 minute ago or less show "just updated"
        if (minutesAgo <= 1)
            template = context.getResource("appointment.date.justUpdated");
        else if (minutesAgo >= 10 && minutesAgo <= 19)
            template = context.getResource("appointment.date.updated_5+minAgo");
            // for *21, *31, .. *51 - минутА
        else if (lastMinute == 1)
            template = context.getResource("appointment.date.updated_1minAgo");
            // for *22-*24, .. *51-*54 - минутЫ
        else if (lastMinute >= 2 && lastMinute <= 4)
            template = context.getResource("appointment.date.updated_2-4minAgo");
            // 5-9 минут, 25-29 минут, 10, 20, 30
        else
            template = context.getResource("appointment.date.updated_5+minAgo");
        return String.format(template, minutesAgo);
    }
}
