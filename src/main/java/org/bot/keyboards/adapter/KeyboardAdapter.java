package org.bot.keyboards.adapter;

import org.bot.commands.CommandResultHandler;
import org.telegram.telegrambots.api.objects.Update;

public interface KeyboardAdapter {

    /**
     * Process user callback (button click)
     *
     * @param handler handler to send result of callback process
     * @param update  contains all information about callback
     * @return True if current keyboard responsible for handle provided callback. False otherwise
     */
    boolean processCallback(CommandResultHandler handler, Update update);
}
