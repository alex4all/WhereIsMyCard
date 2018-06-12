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
        if (minutesAgo <= 1)
            template = context.getResource("appointment.date.justUpdated");
        else if (minutesAgo >= 2 && minutesAgo <= 4)
            template = context.getResource("appointment.date.updated_2-4minAgo");
        else
            template = context.getResource("appointment.date.updated_5+minAgo");
        return String.format(template, minutesAgo);
    }
}
