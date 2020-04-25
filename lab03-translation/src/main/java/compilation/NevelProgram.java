package compilation;

import code.Code;
import context.Stage;
import exception.ParseException;
import grammar.NevelLexer;
import grammar.NevelParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.jetbrains.annotations.NotNull;
import scope.Scope;
import translation.Translator;
import types.Indexer;
import types.Typer;

@SuppressWarnings("FieldCanBeLocal")
public class NevelProgram {
    @NotNull
    private final Typer typer;

    @NotNull
    private final Indexer indexer;

    @NotNull
    private NevelLexer lexer;

    @NotNull
    private NevelParser parser;

    @NotNull
    private final NevelParser.ProgramContext programContext;

    public NevelProgram(@NotNull String code) {
        ParseException.setSource(code);

        this.typer = new Typer();
        this.indexer = new Indexer();

        this.lexer = new NevelLexer(CharStreams.fromString(code));
        this.parser = new NevelParser(new CommonTokenStream(lexer));
        parser.program(typer, indexer, new Scope(), Stage.INDEXATION);
        this.lexer = new NevelLexer(CharStreams.fromString(code));
        this.parser = new NevelParser(new CommonTokenStream(lexer));
        this.programContext = parser.program(typer, indexer, new Scope(), Stage.CONSTRUCTION);
    }

    public Code translate(Translator translator) {
        return translator.translate(programContext);
    }

    @Override
    public String toString() {
        return programContext.toStringTree(parser);
    }
}
