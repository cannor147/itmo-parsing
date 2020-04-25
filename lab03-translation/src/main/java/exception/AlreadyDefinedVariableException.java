package exception;

import context.ParseContext;
import org.jetbrains.annotations.NotNull;

public class AlreadyDefinedVariableException extends ParseException {
    public AlreadyDefinedVariableException(@NotNull String name, @NotNull ParseContext parseContext) {
        super("Variable '" + name + "' is already defined in the scope", parseContext);
    }
}