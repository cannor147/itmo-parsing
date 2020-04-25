package exception;

import context.ParseContext;
import org.jetbrains.annotations.NotNull;

public class CannotResolveFunctionException extends ParseException {
    public CannotResolveFunctionException(@NotNull String name, @NotNull ParseContext parseContext) {
        super("Cannot resolve a function '" + name + "'", parseContext);
    }
}
