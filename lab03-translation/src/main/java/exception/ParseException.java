package exception;

import context.ParseContext;
import org.jetbrains.annotations.NotNull;

public class ParseException extends RuntimeException {
    @NotNull
    public static String source;

    public ParseException(@NotNull String message, @NotNull ParseContext parseContext) {
        super(generateMessage(message, parseContext));
    }

    public ParseException(@NotNull String message, @NotNull ParseContext parseContext, @NotNull Exception cause) {
        super(generateMessage(message, parseContext), cause);
    }

    public static void setSource(@NotNull String source) {
        ParseException.source = source;
    }

    private static String generateMessage(@NotNull String message, @NotNull ParseContext parseContext) {
        return message + System.lineSeparator()
                + "in the line: " + parseContext.getLine() + ", position: " + parseContext.getPosition() + System.lineSeparator()
                + source.split(System.lineSeparator())[parseContext.getLine() - 1] + System.lineSeparator()
                + " ".repeat(parseContext.getPosition()) + "^";
    }
}
