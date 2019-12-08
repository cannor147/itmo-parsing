import expression.Program;
import parser.Parser;

import java.text.ParseException;

public class Main {
    public static void main(String[] args) throws ParseException {
        Parser parser = new Parser();
        Program program = parser.parse(System.in);
        System.out.println(program);
    }
}
