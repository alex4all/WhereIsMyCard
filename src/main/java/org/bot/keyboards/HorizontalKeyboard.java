package org.bot.keyboards;

import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HorizontalKeyboard {
    private List<Button> buttons = new LinkedList<>();

    public void setButtons(List<Button> buttons) {
        this.buttons.addAll(buttons);
    }

    public InlineKeyboardMarkup create() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttonsGreed = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        for (Button button : buttons) {
            rowInline.add(new InlineKeyboardButton()
                    .setText(button.getText())
                    .setCallbackData(button.getCallback()));
        }
        buttonsGreed.add(rowInline);
        markupInline.setKeyboard(buttonsGreed);
        return markupInline;
    }
}
