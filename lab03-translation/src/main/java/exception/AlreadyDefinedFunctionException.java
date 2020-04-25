package exception;

import context.ParseContext;
import org.jetbrains.annotations.NotNull;

public class AlreadyDefinedFunctionException extends ParseException {
    public AlreadyDefinedFunctionException(@NotNull String name, @NotNull ParseContext parseContext) {
        super("Function '" + name + "' is already defined in the scope", parseContext);
    }
}