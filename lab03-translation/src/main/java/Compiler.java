import exception.ParseException;
import grammar.NevelLexer;
import grammar.NevelParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import translation.CTranslator;
import translation.Translator;

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
            NevelParser.ProgramContext program = parser.program();
            System.out.println(program.toStringTree(parser));
            System.out.println();
            Translator translator = new CTranslator();
            System.out.println(translator.translate(program));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /*
        todo: function calls
        todo: custom operators
        todo: arrays
        todo: continue/break/return checking
        todo: for
        todo: super equality
     */
}
