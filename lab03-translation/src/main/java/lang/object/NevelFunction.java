package lang.object;

import lang.mutability.Mutability;
import lang.type.Type;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NevelFunction extends NevelObject {
    @NotNull
    private final List<Type> argumentTypes;

    public NevelFunction(@NotNull String name, @NotNull List<Type> argumentTypes, @NotNull Type resultType) {
        super(name, resultType, Mutability.IMMUTABLE);
        this.argumentTypes = argumentTypes;
    }

    @NotNull
    public List<Type> getArgumentTypes() {
        return argumentTypes;
    }
}
