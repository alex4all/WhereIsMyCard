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
    public static AppointmentDate getDateInfo(String date, AppointmentDate.Type type) throws IOException {
        Map<String, String> arguments = new HashMap<>();
        arguments.put("datakolejki", date);
        arguments.put("kolejka", typeToId(type));

        byte[] body = paramsToRequestBody(arguments);
        int contentLength = body.length;

        HttpURLConnection http = (HttpURLConnection) new URL("http://webqms.pl/puw/ajax_godziny.php").openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setFixedLengthStreamingMode(contentLength);
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        http.connect();
        try (OutputStream os = http.getOutputStream()) {
            os.write(body);
            os.flush();
        }

        String result;
        try (InputStream in = http.getInputStream()) {
            result = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
            result = result.substring(result.indexOf("<")).replaceAll("<.*?>", " ").trim();
            return new AppointmentDate(type, date, result);
        }
    }

    public static String makeAnAppointment(String userName, String email, String date, String time, AppointmentDate.Type type) throws IOException {
        Map<String, String> arguments = new HashMap<>();
        arguments.put("Dalej", "+Dalej+");
        arguments.put("data_biletu", date);
        arguments.put("email", email);
        arguments.put("F", "E1");
        arguments.put("godziny", time);
        arguments.put("kolejka", typeToId(type));
        arguments.put("PESEL", userName);
        arguments.put("zgoda", "1");

        byte[] body = paramsToRequestBody(arguments);
        int contentLength = body.length;

        HttpURLConnection http = (HttpURLConnection) new URL("http://webqms.pl/puw/index.php").openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setFixedLengthStreamingMode(contentLength);
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        http.connect();
        try (OutputStream os = http.getOutputStream()) {
            os.write(body);
            os.flush();
        }

        String response;
        try (InputStream in = http.getInputStream()) {
            response = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
            return substring("<h2>", "</h2>", response);
        }
    }

    public static String cancelAppointment(String userName, String code, String date, String time) throws IOException {
        Map<String, String> arguments = new HashMap<>();
        arguments.put("TCH", time);
        arguments.put("WD", date);
        arguments.put("WK", code);
        arguments.put("WP", userName);

        byte[] body = paramsToRequestBody(arguments);
        int contentLength = body.length;

        HttpURLConnection http = (HttpURLConnection) new URL("http://webqms.pl/puw/anuluj.php").openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setFixedLengthStreamingMode(contentLength);
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        http.connect();
        try (OutputStream os = http.getOutputStream()) {
            os.write(body);
            os.flush();
        }

        String response;
        try (InputStream in = http.getInputStream()) {
            response = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
            return substring("<H2>", "</h3>", response);
        }
    }

    private static String substring(String beginPattern, String endPattern, String source) {
        int beginIndex = source.indexOf(beginPattern);
        int endIndex = source.lastIndexOf(endPattern);
        if (beginIndex != -1 && endIndex != -1) {
            String response = source.substring(beginIndex, endIndex);
            response = response.substring(response.indexOf("<")).replaceAll("<.*?>", " ").trim();
            return response;
        }

        beginPattern = beginPattern.toUpperCase();
        endPattern = endPattern.toUpperCase();
        String sourceInUpperCase = source.toUpperCase();

        beginIndex = sourceInUpperCase.indexOf(beginPattern);
        endIndex = sourceInUpperCase.lastIndexOf(endPattern);
        if (beginIndex != -1 && endIndex != -1) {
            String response = source.substring(beginIndex, endIndex);
            response = response.substring(response.indexOf("<")).replaceAll("<.*?>", " ").trim();
            return response;
        }
        return "Can't parse result provided by server";
    }


    private static byte[] paramsToRequestBody(Map<String, String> arguments) throws UnsupportedEncodingException {
        StringJoiner joiner = new StringJoiner("&");
        for (Map.Entry<String, String> entry : arguments.entrySet())
            joiner.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
        return joiner.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * @param type - kolejka type
     * @return - kolejka id
     */
    public static String typeToId(AppointmentDate.Type type) {
        if (AppointmentDate.Type.ODBIOR.equals(type))
            return "12";
        else if (AppointmentDate.Type.ZLOZENIE.equals(type))
            return "11";
        throw new RuntimeException("Can't map provided type: " + type.toString());
    }

}
