package org.bot;

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

    private static final String URL = "http://webqms.pl/puw/ajax_godziny.php";

    /**
     * Get information about provided date
     */
    public static String getAvailableTime(String date) throws IOException {
        // prepare request body
        byte[] body = createRequestBody(date);
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
        try (InputStream in = http.getInputStream()) {
            String result = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
            result = result.substring(result.indexOf("<")).replaceAll("<.*?>", " ").trim();
            return result;
        }
    }

    /**
     * Create post request body
     */
    private static byte[] createRequestBody(String date) throws UnsupportedEncodingException {
        Map<String, String> arguments = new HashMap<>();
        arguments.put("datakolejki", date);
        arguments.put("kolejka", "12");
        StringJoiner joiner = new StringJoiner("&");
        for (Map.Entry<String, String> entry : arguments.entrySet())
            joiner.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
        return joiner.toString().getBytes(StandardCharsets.UTF_8);
    }

}
