package exception;

import context.ParseContext;
import lang.type.Type;
import org.jetbrains.annotations.NotNull;

public class TypesNotEqualsException extends ParseException {
    public TypesNotEqualsException(@NotNull Type expected, @NotNull Type actual, @NotNull ParseContext parseContext) {
        super("Expected type " + expected.getCode() + ", but found " + actual.getCode() + ".", parseContext);
    }
}
