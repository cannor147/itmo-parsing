package lang.object;

import lang.mutability.Mutability;
import lang.type.Type;
import org.jetbrains.annotations.NotNull;

public class NevelVariable extends NevelObject {
    public NevelVariable(@NotNull String name, @NotNull Type type, @NotNull Mutability mutability) {
        super(name, type, mutability);
    }
}
