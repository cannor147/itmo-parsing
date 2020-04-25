package parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

public class Lexer {
    private static final char END_SYMBOL = '$';
    private final InputStream inputStream;
    private int position;
    private char symbol;
    private Token currentToken;

    public Lexer(String text) throws ParseException {
        this(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }

    public Lexer(InputStream inputStream) throws ParseException {
        this.inputStream = inputStream;
        this.position = 0;
        this.currentToken = Token.START;
        nextSymbol();
    }

    public int getPosition() {
        return position;
    }

    public Token current() {
        return currentToken;
    }

    public Token next() throws ParseException {
        if (symbol == END_SYMBOL) {
            currentToken = Token.END;
        } else if (symbol == '*') {
            currentToken = Token.ASTERISK;
            nextSymbol();
        } else if (symbol == ',') {
            currentToken = Token.COMMA;
            nextSymbol();
        } else if (symbol == ';') {
            currentToken = Token.SEMICOLON;
            nextSymbol();
        } else if (isWordSymbol() && !isDigit()) {
            StringBuilder stringBuilder = new StringBuilder();
            boolean whiteSpace = false;
            while (isWordSymbol() && !whiteSpace) {
                stringBuilder.append(symbol);
                whiteSpace = nextSymbol();
            }

            String word = stringBuilder.toString();
            switch (word) {
                case "int":
                    currentToken = Token.INT;
                    break;
                case "long":
                    currentToken = Token.LONG;
                    break;
                case "unsigned":
                    currentToken = Token.UNSIGNED;
                    break;
                default:
                    currentToken = Token.WORD;
                    currentToken.setName(word);
                    break;
            }
        } else if (currentToken != Token.END) {
            throw new ParseException("Unexpected token '" + symbol + "'.", position);
        }

        return currentToken;
    }

    private boolean nextSymbol() throws ParseException {
        try {
            boolean whiteSpace = false;
            while (true) {
                int x = inputStream.read();
                if (x == -1) {
                    symbol = END_SYMBOL;
                } else {
                    symbol = (char) x;
                    position++;
                }

                if (!Character.isWhitespace(symbol)) {
                    return whiteSpace;
                } else {
                    whiteSpace = true;
                }
            }
        } catch (IOException e) {
            throw new ParseException(e.getMessage(), position);
        }
    }

    private boolean isWordSymbol() {
        return Character.isLetterOrDigit(symbol) || symbol == '_';
    }

    private boolean isDigit() {
        return Character.isDigit(symbol);
    }
}
