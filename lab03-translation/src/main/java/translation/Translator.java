package translation;

import code.Code;
import grammar.NevelParser;

public interface Translator {
    Code translate(NevelParser.ProgramContext ctx);
}
