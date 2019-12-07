package expression;

import java.util.ArrayList;
import java.util.List;

public abstract class Node {
    private final String name;
    private final List<Node> children;

    Node(String name) {
        this(name, new ArrayList<>());
    }

    Node(String name, List<Node> children) {
        this.name = name;
        this.children = children;
    }

    String getName() {
        return name;
    }

    public List<Node> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return name + '(' + children + ')';
    }
}
