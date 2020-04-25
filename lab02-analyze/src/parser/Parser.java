package parser;

import expression.Description;
import expression.Program;
import expression.Type;
import expression.Variable;

import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Parser {
    private Lexer lexer;
    private static final Set<String> TYPE_PREFIXES = new HashSet<>(List.of("long", "unsigned"));

    public Program parse(String text) throws ParseException {
        lexer = new Lexer(text);
        return parseProgram();
    }

    public Program parse(InputStream inputStream) throws ParseException {
        lexer = new Lexer(inputStream);
        return parseProgram();
    }

    private Program parseProgram() throws ParseException {
        List<Description> descriptions = new ArrayList<>();
        int count = 0;

        lexer.next();
        while (lexer.current() != Token.END) {
            if (lexer.current().isWord()) {
                count++;
                descriptions.add(parseDescription(count));
            } else {
                onWrongToken(Token.WORD, lexer.current(), lexer.getPosition());
            }
        }

        if (count == 0) {
            onWrongToken(Token.WORD, Token.END, lexer.getPosition());
        }
        return new Program("Program", descriptions);
    }

    private Description parseDescription(int index) throws ParseException {
        Type type = parseType();
        List<Variable> variables = new ArrayList<>();
        variables.add(parseVariable());

        Token token = lexer.next();
        while (token == Token.COMMA) {
            lexer.next();
            variables.add(parseVariable());
            token = lexer.next();
        }

        if (token != Token.SEMICOLON) {
            onWrongToken(Token.SEMICOLON, token, lexer.getPosition());
        }
        lexer.next();
        return new Description(index, type, variables);
    }

    private Type parseType() throws ParseException {
        StringBuilder typeName = new StringBuilder();
        Token token = lexer.current();

        if (token == Token.UNSIGNED) {
            if (typeName.length() != 0) {
                typeName.append(" ");
            }
            typeName.append(token.getName());
            token = lexer.next();
        }

        for (int i = 0; i < 2; i++) {
            if (token == Token.LONG) {
                if (typeName.length() != 0) {
                    typeName.append(" ");
                }
                typeName.append(token.getName());
                token = lexer.next();
            }
        }

        if (token == Token.INT) {
            if (typeName.length() != 0) {
                typeName.append(" ");
            }
            typeName.append(token.getName());
            token = lexer.next();
        }

        if (typeName.length() == 0) {
            if (token == Token.WORD) {
                typeName.append(token.getName());
                lexer.next();
            } else {
                onWrongToken(Token.WORD, token, lexer.getPosition());
            }
        }

        return new Type(typeName.toString());
    }

    private Variable parseVariable() throws ParseException {
        int pointerCount = 0;
        Token token = lexer.current();
        while (token == Token.ASTERISK) {
            pointerCount++;
            token = lexer.next();
        }

        if (token != Token.WORD) {
            onWrongToken(Token.WORD, token, lexer.getPosition());
        }
        return new Variable(pointerCount, token.getName());
    }

    static void onWrongToken(Token expected, Token actual, int position) throws ParseException {
        throw new ParseException("Expected " + expected.toString() + ", but found " + actual + ".", position);
    }
}
