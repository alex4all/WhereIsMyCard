package org.bot.keyboards;

import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

public class HorizontalKeyboard {
    private Map<String, String> elements = new LinkedHashMap<>();

    public void setElements(Map<String, String> elements) {
        this.elements.putAll(elements);
    }

    public void addElement(String text, String callback) {
        elements.put(text, callback);
    }

    public InlineKeyboardMarkup create() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttonsGreed = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        for (Map.Entry<String, String> element : elements.entrySet()) {
            rowInline.add(new InlineKeyboardButton().setText(element.getKey()).setCallbackData(element.getValue()));
        }
        buttonsGreed.add(rowInline);
        markupInline.setKeyboard(buttonsGreed);
        return markupInline;
    }
}
