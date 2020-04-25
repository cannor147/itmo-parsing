package lang.type;

import code.Code;
import code.Codes;
import org.jetbrains.annotations.NotNull;

public class ArrayType implements Type {
    @NotNull
    private final Type innerType;

    public ArrayType(@NotNull Type innerType) {
        this.innerType = innerType;
    }

    @NotNull
    public Type getInnerType() {
        return innerType;
    }

    @Override
    public boolean equals(@NotNull Type t) {
        return t instanceof ArrayType && innerType.equals(((ArrayType) t).innerType);
    }

    @Override
    public boolean subtypeOf(@NotNull Type t) {
        return t instanceof ArrayType && innerType.subtypeOf(((ArrayType) t).innerType) || t instanceof AnyType;
    }

    @Override
    public @NotNull Code getCode() {
        return new Code(innerType.getCode(), Codes.OPENING_SQUARE_BRACKET, Codes.CLOSING_SQUARE_BRACKET);
    }
}
