package org.bot;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class WhereIsMyCardBot extends TelegramLongPollingBot {
    private static final String COMMAND_FIRST_ODBIOR = "/first_odbior";
    private static final String COMMAND_FIRST_ZLOZENIE = "/first_zlozenie";
    private static final String COMMAND_FIRST_DATE_INFO = "/date_info";
    private static final String COMMAND_HELP = "/help";
    private static final String COMMAND_LINK = "/link";

    private static final String URL = "http://webqms.pl/puw/index.php";

    private static final int DAYS_TO_SCAN = 180;
    private static final long UPDATE_PERIOD = 15 * 60 * 1000;

    private final AppointmentDatesManager datesManager;
    private final String helpMessage;

    private final String botName;
    private final String token;

    public WhereIsMyCardBot(String botName, String token) {
        this.botName = botName;
        this.token = token;
        datesManager = new AppointmentDatesManager(DAYS_TO_SCAN, UPDATE_PERIOD);
        helpMessage = generateHelpMessage();
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            boolean firstObdior = chatText.startsWith(COMMAND_FIRST_ODBIOR);
            boolean firstZlozenie = chatText.startsWith(COMMAND_FIRST_ZLOZENIE);
            if (firstObdior || firstZlozenie) {
                String command = firstObdior ? COMMAND_FIRST_ODBIOR : COMMAND_FIRST_ZLOZENIE;
                AppointmentDate.Type type = firstObdior ? AppointmentDate.Type.ODBIOR : AppointmentDate.Type.ZLOZENIE;
                String daysStr = chatText.substring(command.length()).trim();
                if (daysStr.isEmpty()) {
                    sendFirstAvailableDates(type, 1, chatId);
                    return;
                }
                try {
                    int days = Integer.parseInt(daysStr);
                    sendFirstAvailableDates(type, days, chatId);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    sendMessage("Can't parse count of days from your request. Use 1 by default", chatId, false);
                    sendFirstAvailableDates(type, 1, chatId);
                }
                return;
            }

            if (chatText.startsWith(COMMAND_FIRST_DATE_INFO)) {
                String date = chatText.substring(COMMAND_FIRST_DATE_INFO.length()).trim();
                sendDateInfo(date, chatId);
                return;
            }

            if (chatText.startsWith(COMMAND_HELP)) {
                sendMessage(helpMessage, chatId, true);
            }

            if (chatText.startsWith(COMMAND_LINK)) {
                System.out.println("Send link");
                sendMessage(URL, chatId, false);
            }
        }
    }

    @Override
    public String getBotToken() {
        return token;
    }

    private void sendDateInfo(String date, long chatId) {
        System.out.println("sendDateInfo " + date);
        List<AppointmentDate> dateInfo = null;
        try {
            dateInfo = datesManager.getDateInfo(date);
        } catch (ParseException e) {
            e.printStackTrace();
            sendMessage("Can't parse provided date: " + date, chatId, false);
        }

        // verify thar result is valid. no null elements
        List<AppointmentDate> verifiedDates = new ArrayList<>();
        for (AppointmentDate appDate : dateInfo) {
            if (appDate != null)
                verifiedDates.add(appDate);
        }

        if (verifiedDates.size() == 0)
            sendMessage("No data found for provided date: " + date, chatId, false);

        StringBuilder result = new StringBuilder();
        result.append("<b>").append(verifiedDates.get(0).getDate()).append(":</b>").append(System.lineSeparator());

        for (AppointmentDate appDate : dateInfo)
            result.append(appDate.toMessageWithType()).append(System.lineSeparator());
        sendMessage(result.toString(), chatId, true);
    }

    private void sendFirstAvailableDates(AppointmentDate.Type type, int count, long chatId) {
        System.out.println("sendFirstAvailableDates " + count);
        List<AppointmentDate> datesInfo = datesManager.getFirstAvailableDates(type, count);
        if (datesInfo.size() == 0)
            sendMessage("No data found for", chatId, false);
        StringBuilder result = new StringBuilder();
        for (AppointmentDate dayInfo : datesInfo)
            result.append(dayInfo.toMessageWithDate()).append(System.lineSeparator());

        sendMessage(result.toString(), chatId, true);
    }

    private void sendMessage(String text, long chatId, boolean htmlEnable) {
        SendMessage message = new SendMessage();
        message.enableHtml(htmlEnable);
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private String generateHelpMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append("<b>/first_odbior [1,10]</b> - get list of first available dates. You can provide count argument").append(System.lineSeparator());
        builder.append("<b>/first_zlozenie [1,10]</b> - get list of first available dates. You can provide count argument").append(System.lineSeparator());
        builder.append("<b>/date_info yyyy-MM-dd</b> - get information about provided date. You have to provide date argument").append(System.lineSeparator());
        builder.append("<b>/link</b> - get Urzad URL").append(System.lineSeparator());
        builder.append("<b>/help</b> - list of available commands");
        return builder.toString();
    }
}
