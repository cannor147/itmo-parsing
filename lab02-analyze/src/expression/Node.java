package expression;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class Node {
    @NotNull
    private final String name;

    @NotNull
    private final List<Node> children;

    Node(String name) {
        this(name, new ArrayList<>());
    }

    Node(@NotNull String name, @NotNull List<Node> children) {
        this.name = name;
        this.children = children;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    protected List<Node> getChildren() {
        return children;
    }

    public void printTree() {
        System.out.println(findTree(0));
    }

    protected String getTabs(int x) {
        return "|\t".repeat(x);
    }

    protected abstract String findTree(int x);

    @Override
    public String toString() {
        return name + '(' + children + ')';
    }
}
