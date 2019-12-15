package parser;

import java.util.HashMap;
import java.util.Map;

public enum Token {
    START, END,
    WORD, ASTERISK, COMMA, SEMICOLON,
    LONG("long"), UNSIGNED("unsigned"), INT("int");

    Token() {
        // No operations;
    }

    Token(String name) {
        setName(name);
    }

    private final Map<String, String> parameters = new HashMap<>();

    public String getName() {
        return parameters.get("name");
    }

    public void setName(String name) {
        parameters.put("name", name);
    }

    public boolean isWord() {
        return this == WORD || this == LONG || this == UNSIGNED || this == INT;
    }
}
