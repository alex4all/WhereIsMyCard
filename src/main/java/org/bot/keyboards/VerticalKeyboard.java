package org.bot.keyboards;

import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VerticalKeyboard {

    private String callbackPrefix;
    private List<Object> elements;

    public VerticalKeyboard callbackPrefix(String callbackPrefix) {
        this.callbackPrefix = callbackPrefix;
        return this;
    }

    public VerticalKeyboard elements(List<Object> elements) {
        this.elements = elements;
        return this;
    }

    public VerticalKeyboard elements(Object[] elements) {
        this.elements = Arrays.asList(elements);
        return this;
    }

    public InlineKeyboardMarkup create() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttonsGreed = new ArrayList<>();
        for (Object element : elements) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(new InlineKeyboardButton().setText(element.toString()).setCallbackData(callbackPrefix + element.toString()));
            buttonsGreed.add(rowInline);
        }
        markupInline.setKeyboard(buttonsGreed);
        return markupInline;
    }
}
