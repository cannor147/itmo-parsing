package expression;

import java.util.ArrayList;
import java.util.List;

public class Program extends Node {
    Program(String name, List<Description> descriptions) {
        super(name, new ArrayList<>(descriptions));
    }

}
