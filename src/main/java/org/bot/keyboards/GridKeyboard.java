package org.bot.keyboards;

import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GridKeyboard {
    private List<List<Button>> buttonsGrid = new LinkedList<>();

    public void addRow(List<Button> buttonsRow) {
        buttonsGrid.add(buttonsRow);
    }

    public void addGrid(List<List<Button>> buttonsGrid) {
        this.buttonsGrid.addAll(buttonsGrid);
    }

    public void clear() {
        buttonsGrid.clear();
    }

    public InlineKeyboardMarkup build() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttonsGreed = new ArrayList<>();
        for (List<Button> buttonsRow : buttonsGrid) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            for (Button button : buttonsRow) {
                rowInline.add(new InlineKeyboardButton()
                        .setText(button.getText())
                        .setCallbackData(button.getCallback()));
            }
            buttonsGreed.add(rowInline);
        }
        markupInline.setKeyboard(buttonsGreed);
        return markupInline;
    }
}
