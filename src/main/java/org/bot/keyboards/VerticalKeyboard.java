package org.bot.keyboards;

import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class VerticalKeyboard {
    private List<Button> buttons = new LinkedList<>();

    public void setButtons(List<Button> buttons) {
        this.buttons.addAll(buttons);
    }

    public InlineKeyboardMarkup build() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttonsGreed = new ArrayList<>();
        for (Button button : buttons) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(new InlineKeyboardButton()
                    .setText(button.getText())
                    .setCallbackData(button.getCallback()));
            buttonsGreed.add(rowInline);
        }
        markupInline.setKeyboard(buttonsGreed);
        return markupInline;
    }
}
