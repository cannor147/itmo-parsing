import expression.Program;
import parser.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;

public class Main {
    public static void main(String[] args) throws IOException, ParseException {
        Parser parser = new Parser();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        StringBuilder expr = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            expr.append(" ").append(line);
        }

        Program program = parser.parse(expr.toString());
        System.out.println(program);
    }
}
