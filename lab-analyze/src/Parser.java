import expression.Program;

import java.io.InputStream;
import java.text.ParseException;

public class Parser {
    private Lexer lexer;

    public Program parse(String text) throws ParseException {
        lexer = new Lexer(text);
        return parseProgram();
    }

    public Program parse(InputStream inputStream) throws ParseException {
        lexer = new Lexer(inputStream);
        return parseProgram();
    }

    private Program parseProgram() {
        return null;
    }
}
