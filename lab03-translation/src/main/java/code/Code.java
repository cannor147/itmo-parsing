package code;

import java.util.HashMap;

public class Code {
    private static final Code TAB = new Code("\t");
    private static final Code LINE_BREAK = new Code(System.lineSeparator());

    private final StringBuilder stringBuilder;

    public Code(String text) {
        this.stringBuilder = new StringBuilder(text);
    }

    public Code(Code... codes) {
        this.stringBuilder = new StringBuilder();
        add(codes);
    }

    public void add(Code... codes) {
        for (Code c : codes) {
            stringBuilder.append(c.stringBuilder);
        }
    }

    public void addln(Code... codes) {
        add(codes);
        add(LINE_BREAK);
    }

    public void tabulate(int count) {
        for (int i = 0; i < count; i++) {
            add(TAB);
        }
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }
}
