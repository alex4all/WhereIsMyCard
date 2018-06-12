package org.bot.keyboards;

public class Button {
    public String text;
    public String callback;

    public Button(String text, String callback) {
        this.text = text;
        this.callback = callback;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    @Override
    public String toString() {
        return text + ":" + callback;
    }
}
