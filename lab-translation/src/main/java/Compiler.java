import exception.ParseException;
import grammar.NevelLexer;
import grammar.NevelParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Compiler {

    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        StringBuilder codeBuilder = new StringBuilder();
        while (true) {
            String line = "";
            try {
                line = reader.readLine();
                if (line == null) {
                    break;
                } else {
                    codeBuilder.append(System.lineSeparator());
                }
            } catch (IOException e) {
                System.err.println("ERROR 500");
                return;
            }
            codeBuilder.append(line);
        }

        try {
            String code = codeBuilder.toString();
            ParseException.externalSource = code;
            NevelLexer lexer = new NevelLexer(CharStreams.fromString(code));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            NevelParser parser = new NevelParser(tokens);
            ParseTree tree = parser.program();
            System.out.println(tree.toStringTree(parser));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    static void ensureSubtype(Type expected, Type actual) {
        if ((expected.getCode() & actual.getCode()) != actual.getCode()) {
            throw new RuntimeException("Lol kek");
        }
    }

    static void ensureEquals(Type expected, Type actual) {
        if (expected.getCode() != actual.getCode()) {
            throw new RuntimeException("Lol kek");
        }
    }

    enum Type {
        BOOL(1),
        BYTE(2),
        SHORT(4),
        INT(8),
        LONG(16),
        FLOAT(32),
        DOUBLE(64),
        CHAR(128),
        STRING(256),

        INTEGER(30),
        NUMBER(126),
        PRIMITIVE(254),
        ANY(Integer.MAX_VALUE);

        private final int code;

        Type(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
