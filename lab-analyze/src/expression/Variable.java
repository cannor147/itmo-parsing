package expression;

public class Variable extends Node {
    public Variable(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return getName();
    }
}
