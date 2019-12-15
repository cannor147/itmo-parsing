package expression;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Description extends Node {
    public Description(int index, @NotNull Type type, @NotNull List<Variable> variables) {
        super(Integer.toString(index), Stream.concat(Stream.of(type), variables.stream()).collect(Collectors.toList()));
    }

    public @NotNull Type getType() {
        return (Type) getChildren().get(0);
    }

    public @NotNull List<Variable> getVariables() {
        return getChildren().subList(1, getChildren().size()).stream()
                .map(node -> (Variable) node).collect(Collectors.toUnmodifiableList());
    }

    @Override
    protected String findTree(int x) {
        StringBuilder stringBuilder = new StringBuilder(getTabs(x));
        stringBuilder.append("@Description ").append(getName()).append(System.lineSeparator());
        stringBuilder.append(getType().findTree(x + 1));

        for (Variable variable : getVariables()) {
            stringBuilder.append(System.lineSeparator());
            stringBuilder.append(variable.findTree(x + 1));
        }
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return getType().toString() + " " + getVariables().stream().map(Node::toString).collect(Collectors.joining(", ")) + ";";
    }
}
