package exception;

import context.ParseContext;
import org.jetbrains.annotations.NotNull;

public class CannotResolveVariableException extends ParseException {
    public CannotResolveVariableException(@NotNull String name, @NotNull ParseContext parseContext) {
        super("Cannot resolve a variable '" + name + "'", parseContext);
    }
}
