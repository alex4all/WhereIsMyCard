package org.bot;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.List;

public class WhereIsMyCardBot extends TelegramLongPollingBot {
    private static final String COMMAND_FIRST_AVAILABLE_DATES = "/firstavailabledates";
    private static final String COMMAND_FIRST_AVAILABLE_DATE = "/firstavailabledate";
    private static final String COMMAND_FIRST_DATE_INFO = "/dateinfo";
    private static final String COMMAND_HELP = "/help";
    private static final String COMMAND_LINK = "/link";

    private static final String URL = "http://webqms.pl/puw/index.php";

    private static final int DAYS_TO_SCAN = 180;
    private static final long UPDATE_PERIOD = 15 * 60 * 1000;

    private final AppointmentDatesManager datesManager;
    private final String helpMessage;
    private final String token;

    public WhereIsMyCardBot() {
        token = System.getenv("telegram_bot_token");
        if (token == null)
            throw new RuntimeException("Token can't be null");
        datesManager = new AppointmentDatesManager(DAYS_TO_SCAN, UPDATE_PERIOD);
        helpMessage = generateHelpMessage();
    }

//    private String readHelpMessage() {
//        try {
//            StringBuilder helpBuilder = new StringBuilder();
//            BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/help.txt"));
//            String line = reader.readLine();
//            while (line != null) {
//                helpBuilder.append(line).append(System.lineSeparator());
//                line = reader.readLine();
//            }
//            return helpBuilder.toString();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "Can't read help file";
//        }
//    }

    private String generateHelpMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append("/firstavailabledate - get first available date").append(System.lineSeparator());
        builder.append("/firstavailabledates [1,10] - get list of first available dates. You have to provide number " +
                "argument - how many days you want to print").append(System.lineSeparator());
        builder.append("/dateinfo yyyy-MM-dd - get information about provided date. You have to provide date argument")
                .append(System.lineSeparator());
        builder.append("/link - get Urzad URL");
        return builder.toString();
    }

    @Override
    public String getBotUsername() {
        return "WhereIsMyCardBot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (chatText.startsWith(COMMAND_FIRST_AVAILABLE_DATES)) {
                String daysStr = chatText.substring(COMMAND_FIRST_AVAILABLE_DATES.length()).trim();
                try {
                    int days = Integer.parseInt(daysStr);
                    sendFirstAvailableDates(days, chatId);
                } catch (NumberFormatException e) {
                    sendMessage("Can't get count of days from your request. Use 5 by default", chatId);
                    sendFirstAvailableDates(5, chatId);
                    e.printStackTrace();
                }
                return;
            }

            if (chatText.startsWith(COMMAND_FIRST_AVAILABLE_DATE)) {
                sendFirstAvailableDates(1, chatId);
                return;
            }

            if (chatText.startsWith(COMMAND_FIRST_DATE_INFO)) {
                String date = chatText.substring(COMMAND_FIRST_DATE_INFO.length()).trim();
                sendDateInfo(date, chatId);
                return;
            }

            if (chatText.startsWith(COMMAND_HELP)) {
                sendMessage(helpMessage, chatId);
            }

            if (chatText.startsWith(COMMAND_LINK)) {
                System.out.println("Send link");
                sendMessage(URL, chatId);
            }
        }
    }

    @Override
    public String getBotToken() {
        return token;
    }

    private void sendDateInfo(String date, long chatId) {
        System.out.println("sendDateInfo " + date);
        AppointmentDate dateInfo = datesManager.getDateInfo(date);
        String info;
        if (dateInfo != null)
            info = dateInfo.toString();
        else
            info = "No data found for provided date: " + date;
        sendMessage(info, chatId);
    }

    private void sendFirstAvailableDates(int count, long chatId) {
        System.out.println("sendFirstAvailableDates " + count);
        List<AppointmentDate> datesInfo = datesManager.getFirstAvailableDates(count);
        if (datesInfo.size() == 0)
            sendMessage("No data found for", chatId);
        StringBuilder result = new StringBuilder();
        for (AppointmentDate dayInfo : datesInfo)
            result.append(dayInfo.toString()).append(System.lineSeparator());

        sendMessage(result.toString(), chatId);
    }

    private void sendMessage(String text, long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
