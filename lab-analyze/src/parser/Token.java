package parser;

import java.util.HashMap;
import java.util.Map;

public enum Token {
    START, END,
    WORD, ASTERISK, COMMA, SEMICOLON;

    private final Map<String, String> parameters = new HashMap<>();

    public String getName() {
        return parameters.get("name");
    }

    public void setName(String name) {
        parameters.put("name", name);
    }
}
