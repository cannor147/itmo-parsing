package lang.object;

import lang.mutability.Mutability;
import lang.type.Type;
import org.jetbrains.annotations.NotNull;

public abstract class NevelObject {
    @NotNull
    private final String name;

    @NotNull
    private final Type type;

    @NotNull
    private final Mutability mutability;

    protected NevelObject(@NotNull String name, @NotNull Type type, @NotNull Mutability mutability) {
        this.name = name;
        this.type = type;
        this.mutability = mutability;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public Type getType() {
        return type;
    }

    @NotNull
    public Mutability getMutability() {
        return mutability;
    }
}
