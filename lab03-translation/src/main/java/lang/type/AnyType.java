package lang.type;

import code.Code;
import code.Codes;
import org.jetbrains.annotations.NotNull;

public class AnyType implements Type {
    private static final AnyType INSTANCE = new AnyType();

    public static AnyType getInstance() {
        return INSTANCE;
    }

    private AnyType() {
        // No operations.
    }

    @Override
    public boolean equals(@NotNull Type t) {
        return t instanceof AnyType;
    }

    @Override
    public boolean subtypeOf(@NotNull Type t) {
        return t instanceof AnyType;
    }

    @Override
    public @NotNull Code getCode() {
        return Codes.IMAGINARY_CODE;
    }
}
