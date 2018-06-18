package org.bot.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CallbackData {
    private static final String SEPARATOR = "&";
    private static final String EQUALS = "=";

    private String command;
    private String event;
    private Map<String, String> args = new HashMap<>();

    /**
     * @param query string row with following pattern: Command&Event&Arg1=Value1&Arg2=Value2....
     */
    public CallbackData(String query) {
        String[] queryParts = query.split(SEPARATOR);
        if (queryParts.length < 2)
            return;
        command = queryParts[0];
        event = queryParts[1];

        if (queryParts.length == 2) {
            args = new HashMap<>(0);
            return;
        }

        String[] argsArray = Arrays.copyOfRange(queryParts, 3, queryParts.length);
        args = new HashMap<>(argsArray.length);
        for (String param : argsArray) {
            String[] argValue = param.split(EQUALS);
            args.put(argValue[0], argValue[1]);
        }
    }

    /**
     * Generate callback query
     *
     * @return callback query with following pattern: Command&Event&Arg1=Value1&Arg2=Value2....
     */
    public String toCallbackQuery() {
        if (command == null || event == null)
            throw new RuntimeException("Command or event can't be null. Command: " + command + "; Event: " + event);
        StringBuilder builder = new StringBuilder();
        builder.append(command).append(SEPARATOR).append(event);
        if (args.size() == 0)
            return builder.toString();

        for (Map.Entry<String, String> arg : args.entrySet())
            builder.append(SEPARATOR).append(arg.getKey()).append(EQUALS).append(arg.getValue());

        return builder.toString();
    }

    public String getCommand() {
        return command;
    }

    public String getEvent() {
        return event;
    }

    public String getArg(String key) {
        return args.get(key);
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public void addArg(String name, String value) {
        this.args.put(name, value);
    }
}
