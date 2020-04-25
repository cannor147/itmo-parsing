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
    protected String findTree(int x) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < pointers; i++) {
            stringBuilder.append(getTabs(x + i)).append("@Pointer *").append(System.lineSeparator());
        }
        stringBuilder.append(getTabs(x + pointers)).append("@Variable ").append(getName());
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return "*".repeat(pointers) + getName();
    }
}
