package lang.type;

import code.Code;
import code.Codes;
import org.jetbrains.annotations.NotNull;

public enum PrimitiveType implements Type {
    VOID(Integer.MIN_VALUE, Codes.VOID),
    BOOL(1, Codes.BOOL),
    BYTE(2, Codes.BYTE),
    SHORT(4, Codes.SHORT),
    INT(8, Codes.INT),
    LONG(16, Codes.LONG),
    FLOAT(32, Codes.FLOAT),
    DOUBLE(64, Codes.DOUBLE),
    CHAR(128, Codes.CHAR),

    ALGEBRAIC(31),
    INTEGER(30),
    NUMBER(126),
    PRIMITIVE(-1);

    private final int id;

    @NotNull
    private final Code code;

    PrimitiveType(int id, @NotNull Code code) {
        this.id = id;
        this.code = code;
    }

    PrimitiveType(int id) {
        this(id, Codes.IMAGINARY_CODE);
    }

    @Override
    public boolean equals(@NotNull Type t) {
        return t instanceof PrimitiveType && id == ((PrimitiveType) t).id;
    }

    @Override
    public boolean subtypeOf(@NotNull Type t) {
        return t instanceof PrimitiveType && (id & ((PrimitiveType) t).id) == id || t instanceof AnyType;
    }

    public int getId() {
        return id;
    }

    @NotNull
    @Override
    public Code getCode() {
        return code;
    }
}
