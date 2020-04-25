package lang.type;

import code.Code;
import org.jetbrains.annotations.NotNull;

public interface Type {
    boolean equals(@NotNull Type t);

    boolean subtypeOf(@NotNull Type t);

    @NotNull
    Code getCode();
}
