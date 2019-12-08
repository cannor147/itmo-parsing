package parser;

import expression.Description;
import expression.Program;
import expression.Type;
import expression.Variable;

import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

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

    private Program parseProgram() throws ParseException {
        List<Description> descriptions = new ArrayList<>();
        int count = 0;

        lexer.next();
        while (lexer.current() != Token.END) {
            if (lexer.current() == Token.WORD) {
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
            variables.add(parseVariable());
            token = lexer.next();
        }

        if (token != Token.SEMICOLON) {
            onWrongToken(Token.SEMICOLON, token, lexer.getPosition());
        }
        lexer.next();
        return new Description(index, type, variables);
    }

    private Type parseType() {
        Token token = lexer.current();
        return new Type(token.getName());
    }

    private Variable parseVariable() throws ParseException {
        int pointerCount = 0;
        Token token = lexer.next();
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
