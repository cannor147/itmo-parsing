package compilation;

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
        String code = codeBuilder.toString();

        try {
            NevelProgram nevelProgram = new NevelProgram(code);
            System.out.println(nevelProgram);
            System.out.println();
            Translator translator = new CTranslator();
            System.out.println(nevelProgram.translate(translator));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /*
        todo: custom operators
        todo: arrays
        todo: for
        todo: super equality
     */
}
