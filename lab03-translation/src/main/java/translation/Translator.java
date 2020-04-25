package translation;

import grammar.NevelParser;

public interface Translator {
    Code translate(NevelParser.ProgramContext ctx);
}
