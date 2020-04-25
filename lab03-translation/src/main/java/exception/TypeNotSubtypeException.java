package exception;

import context.ParseContext;
import lang.type.Type;
import org.jetbrains.annotations.NotNull;

public class TypeNotSubtypeException extends ParseException {
    public TypeNotSubtypeException(@NotNull Type expected, @NotNull Type actual, @NotNull ParseContext parseContext) {
        super("Expected subtype of " + expected.getCode() + ", but found " + actual.getCode() + ".", parseContext);
    }
}
