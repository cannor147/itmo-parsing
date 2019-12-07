import java.util.HashMap;
import java.util.Map;

public enum Token {
    START, END,
    WORD, ASTERISK, COMMA, SEMICOLON;

    private final Map<String, String> parameters = new HashMap<>();

    public void setName(String name) {
        parameters.put("name", name);
    }

    public String getName(String name) {
        return parameters.get(name);
    }
}
