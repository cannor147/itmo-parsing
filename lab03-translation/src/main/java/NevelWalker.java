import grammar.NevelBaseListener;
import grammar.NevelParser;

public class NevelWalker extends NevelBaseListener {
    public void enterR(NevelParser.ProgramContext ctx ) {
        System.out.println( "Entering R : " + ctx.getText() );
    }

    public void exitR(NevelParser.ProgramContext ctx ) {
        System.out.println( "Exiting R" );
    }
}