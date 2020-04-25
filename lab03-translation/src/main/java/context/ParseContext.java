package context;

import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;
import scope.Scope;

@SuppressWarnings("unused")
public class ParseContext {
    private final int line;

    private final int position;

    @NotNull
    private final Stage stage;

    @NotNull
    private final Scope scope;

    public ParseContext(int line, int position, @NotNull Scope scope, @NotNull Stage stage) {
        this.line = line;
        this.position = position;
        this.scope = scope;
        this.stage = stage;
    }

    public ParseContext(@NotNull ParserRuleContext _localctx, @NotNull Scope scope, @NotNull Stage stage) {
        this(_localctx.start.getLine(), _localctx.start.getCharPositionInLine(), scope, stage);
    }

    public int getLine() {
        return line;
    }

    public int getPosition() {
        return position;
    }

    @NotNull
    public Scope getScope() {
        return scope;
    }

    @NotNull
    public Stage getStage() {
        return stage;
    }

}
