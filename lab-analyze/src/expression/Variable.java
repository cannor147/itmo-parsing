package expression;

import org.jetbrains.annotations.NotNull;

public class Variable extends Node {
    private final int pointers;

    public Variable(int pointers, @NotNull String name) {
        super(name);
        this.pointers = pointers;
    }

    public int getPointers() {
        return pointers;
    }

    @Override
    public String toString() {
        return "*".repeat(pointers) + getName();
    }
}
