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
    public String toString() {
        return getDescriptions().stream().map(Description::toString).collect(Collectors.joining(System.lineSeparator()));
    }
}
