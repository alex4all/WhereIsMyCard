package org.bot;

import java.util.ResourceBundle;

public class AppData {
    private static final AppData INSTANCE = new AppData();
    private ResourceBundle messages;

    private AppData() {
        messages = ResourceBundle.getBundle("messages");
    }

    public static AppData getInstance() {
        return INSTANCE;
    }

    public String getMessage(String messageCode) {
        return messages.getString(messageCode);
    }
}
