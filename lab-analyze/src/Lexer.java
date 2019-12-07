import jdk.internal.util.xml.impl.ReaderUTF8;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

public class Lexer {
    private Reader reader;
    private int position;
    private char symbol;
    private Token currentToken;

    public Lexer(String text) throws ParseException {
        this(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }

    public Lexer(InputStream inputStream) throws ParseException {
        this.reader = new ReaderUTF8(inputStream);
        this.position = 0;
        this.currentToken = Token.START;
        nextSymbol();
    }

    public Token next() throws ParseException {
        if (symbol == '*') {
            currentToken = Token.ASTERISK;
            nextSymbol();
        } else if (symbol == ',') {
            currentToken = Token.COMMA;
            nextSymbol();
        } else if (symbol == ';') {
            currentToken = Token.SEMICOLON;
            nextSymbol();
        } else if (isWordSymbol()) {
            StringBuilder stringBuilder = new StringBuilder();
            while (isWordSymbol()) {
                stringBuilder.append(symbol);
                nextSymbol();
            }
            currentToken = Token.WORD;
            currentToken.setName(stringBuilder.toString());
        } else if (currentToken != Token.END) {
            throw new ParseException("Can't parse symbol '" + symbol + "'", position);
        }

        return currentToken;
    }

    private void nextSymbol() throws ParseException {
        try {
            boolean f = true;
            while (f) {
                int x = reader.read();
                if (x == -1) {
                    currentToken = Token.END;
                    symbol = '$';
                } else {
                    symbol = (char) x;
                    position++;
                }

                if (!Character.isWhitespace(symbol)) {
                    f = false;
                }
            }
        } catch (IOException e) {
            throw new ParseException(e.getMessage(), position);
        }
    }

    private boolean isWordSymbol() {
        return Character.isLetterOrDigit(symbol) || symbol == '_';
    }
}
