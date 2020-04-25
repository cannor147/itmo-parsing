package exception;

import context.ParseContext;
import org.jetbrains.annotations.NotNull;

public class CannotAssignToConstVariableException extends ParseException {
    public CannotAssignToConstVariableException(@NotNull String name, @NotNull ParseContext parseContext) {
        super("Cannot assign a value to const variable '" + name + "'", parseContext);
    }
}
