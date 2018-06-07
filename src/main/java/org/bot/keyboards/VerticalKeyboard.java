package org.bot.keyboards;

import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VerticalKeyboard {
    private Map<String, String> elements = new LinkedHashMap<>();

    public void setElements(Map<String, String> elements) {
        this.elements.putAll(elements);
    }

    public void addElement(String text, String callback) {
        elements.put(text, callback);
    }

    public InlineKeyboardMarkup butid() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttonsGreed = new ArrayList<>();
        for (Map.Entry<String, String> element : elements.entrySet()) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(new InlineKeyboardButton().setText(element.getKey()).setCallbackData(element.getValue()));
            buttonsGreed.add(rowInline);
        }
        markupInline.setKeyboard(buttonsGreed);
        return markupInline;
    }
}
