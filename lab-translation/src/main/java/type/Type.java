package type;

import translation.Code;

public enum Type {
    BOOL(1, "bool"),
    BYTE(2, "signed char"),
    SHORT(4, "signed short int"),
    INT(8, "signed int"),
    LONG(16, "signed long long int"),
    FLOAT(32, "float"),
    DOUBLE(64, "double"),
    CHAR(128, "unsigned char"),
    STRING(256, "char*"),
    VOID(Integer.MIN_VALUE, "void"),

    ALGEBRAIC(31, null),
    INTEGER(30, null),
    NUMBER(126, null),
    PRIMITIVE(254, null),
    ANY(Integer.MAX_VALUE, null);

    private final int code;
    private final String CName;

    Type(int code, String CName) {
        this.code = code;
        this.CName = CName;
    }

    public int getCode() {
        return code;
    }

    public Code getCCode() {
        return new Code(CName);
    }
}
