package expression;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Program extends Node {
    public Program(@NotNull String name, @NotNull List<Description> descriptions) {
        super(name, new ArrayList<>(descriptions));
    }

    public @NotNull List<Description> getDescriptions() {
        return getChildren().stream().map(node -> (Description) node).collect(Collectors.toUnmodifiableList());
    }

    @Override
    protected String findTree(int x) {
        StringBuilder stringBuilder = new StringBuilder(getTabs(x));
        stringBuilder.append("@Program ").append(getName());

        for (Description description : getDescriptions()) {
            stringBuilder.append(System.lineSeparator()).append(description.findTree(x + 1));
        }
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return getDescriptions().stream().map(Description::toString).collect(Collectors.joining(System.lineSeparator()));
    }
}
