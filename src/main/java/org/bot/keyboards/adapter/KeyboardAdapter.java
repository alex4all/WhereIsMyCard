package org.bot.keyboards.adapter;

import org.bot.commands.Context;
import org.telegram.telegrambots.api.objects.Update;

public interface KeyboardAdapter {

    /**
     * Process user callback (button click)
     *
     * @param context request context
     * @param update  contains all information about callback
     * @return True if current keyboard responsible for handle provided callback. False otherwise
     */
    boolean processCallback(Context context, Update update);
}
