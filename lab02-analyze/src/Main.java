import expression.Program;
import parser.Parser;

import java.text.ParseException;

public class Main {
    public static void main(String[] args) {
        Parser parser = new Parser();
        try {
            Program program = parser.parse(System.in);
            System.out.println(program);
            System.out.println();
            program.printTree();
        } catch (ParseException e) {
            System.err.println("Error on position " + e.getErrorOffset() + ".");
            System.err.println(e.getMessage());
        }
    }
}
