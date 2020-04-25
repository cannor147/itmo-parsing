package lang.type;

import code.Code;
import code.Codes;
import org.jetbrains.annotations.NotNull;

public class StringType extends ArrayType implements Type {
    private static final StringType INSTANCE = new StringType();

    public static StringType getInstance() {
        return INSTANCE;
    }

    private StringType() {
        super(PrimitiveType.CHAR);
    }

    @NotNull
    @Override
    public Code getCode() {
        return Codes.STRING;
    }
}
