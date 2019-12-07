package expression;

public class Type extends Node {
    public Type(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return getName();
    }
}
