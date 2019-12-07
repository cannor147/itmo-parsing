package expression;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Description extends Node {
    public Description(int index, Type type, List<Variable> variables) {
        super(Integer.toString(index), Stream.concat(variables.stream(), Stream.of(type)).collect(Collectors.toList()));
    }
}
