package util;

import java.util.HashMap;
import java.util.Map;

public class ParsedCommand {
    private final String action;
    private final Map<String, String> args;

    public ParsedCommand(String action) {
        this.action = action;
        this.args = new HashMap<>();
    }

    public String getAction() {
        return action;
    }

    public void addArg(String flag, String value) {
        args.put(flag, value);
    }


    public String getArg(String flag) {
        return args.get(flag);
    }

    public boolean hasFlag(String flag) {
        return args.containsKey(flag);
    }
}