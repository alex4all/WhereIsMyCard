package org.bot.net;

import org.bot.appointment.AppointmentDate;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class ServerAPI {
    private static final String DATE_PARAM = "datakolejki";
    private static final String KOLEJKA_PARAM = "kolejka";
    private static final String URL = "http://webqms.pl/puw/ajax_godziny.php";

    public static AppointmentDate getDateInfo(String date, AppointmentDate.Type type) throws IOException {
        // prepare request body
        byte[] body = createRequestBody(date, type);
        int contentLength = body.length;

        // prepare and send post request
        HttpURLConnection http = (HttpURLConnection) new URL(URL).openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setFixedLengthStreamingMode(contentLength);
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        http.connect();
        try (OutputStream os = http.getOutputStream()) {
            os.write(body);
            os.flush();
        }

        // parse result
        String result;
        try (InputStream in = http.getInputStream()) {
            result = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
            result = result.substring(result.indexOf("<")).replaceAll("<.*?>", " ").trim();
            return new AppointmentDate(type, date, result);
        }

    }

    /**
     * Create post request body
     */
    private static byte[] createRequestBody(String date, AppointmentDate.Type type) throws UnsupportedEncodingException {
        Map<String, String> arguments = new HashMap<>();
        arguments.put(DATE_PARAM, date);
        arguments.put(KOLEJKA_PARAM, mapTypeToId(type));
        StringJoiner joiner = new StringJoiner("&");
        for (Map.Entry<String, String> entry : arguments.entrySet())
            joiner.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
        return joiner.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * @param type - kolejka type
     * @return - kolejka id
     */
    public static String mapTypeToId(AppointmentDate.Type type) {
        if (AppointmentDate.Type.ODBIOR.equals(type))
            return "12";
        else if (AppointmentDate.Type.ZLOZENIE.equals(type))
            return "11";
        throw new RuntimeException("Can't map provided type: " + type.toString());
    }

}
