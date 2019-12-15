package exception;

public class ParseException extends RuntimeException {
    public static String externalSource;

    public ParseException(String message, int line, int position, String source) {
        super(generateMessage(message, line, position, source));
    }

    public ParseException(String message, int line, int position, String source, Exception cause) {
        super(generateMessage(message, line, position, source), cause);
    }

    private static String generateMessage(String message, int line, int position, String source) {
        return message + System.lineSeparator()
                + "in the line: " + line + ", position: " + position + System.lineSeparator()
                + externalSource.split(System.lineSeparator())[line - 1] + System.lineSeparator()
                + " ".repeat(position) + "^";
    }
}
