package parser;

import expression.Description;
import expression.Program;
import expression.Type;
import expression.Variable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

class ParserTest {
    private static final String SEPARATOR = "============================";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";

    private static final Random random = new Random();
    private static final Parser parser = new Parser();

    @Test
    void testSimple() {
        internalTest("", null);
        internalTest("(()(()))", null);
        internalTest("2 + 3 * 4", null);

        internalTest("int x", null);
        internalTest("int x,", null);
        internalTest("int x;", constructSimpleProgram("int", "x"));
        internalTest("x", null);
        internalTest("x,", null);
        internalTest("x;", null);

        internalTest("int 1;", null);
        internalTest("1 x;", null);

        internalTest("int *x;", constructSimpleProgram("int", "*x"));
        internalTest("int *x", null);
        internalTest("int x*;", null);
        internalTest("int ****;", null);
        internalTest("int ****,", null);
        internalTest("int ****1", null);

        internalTest(", a, *b, c;", null);
        internalTest("int int, int, int;", constructSimpleProgram("int", "int", "int", "int"));
        internalTest("string _string;", constructSimpleProgram("string", "_string"));
        internalTest("_string string;", constructSimpleProgram("_string", "string"));

        Program program88 = constructSimpleProgram("int", "*******************x88");
        internalTest("int *******************x88;", program88);
        internalTest("int*******************x88;", program88);
        internalTest("int******************* x88;", program88);

        Program program89 = constructSimpleProgram("int", "*******************x88", "x89");
        internalTest("int *******************x88, x89;", program89);
        internalTest("int*******************x88, x89;", program89);
        internalTest("int******************* x88, x89;", program89);

        Program program90 = constructSimpleProgram("int", "x90");
        internalTest("int         x90;", program90);
        internalTest("int" + System.lineSeparator() + "x90;", program90);
        internalTest("int\tx90;", program90);
        internalTest("\tint x90;", program90);
        internalTest("intx90;", null);

        internalTest("int x,y,z;", constructSimpleProgram("int", "x", "y", "z"));
        internalTest("int*x,y,z;", constructSimpleProgram("int", "*x", "y", "z"));
    }

    @Test
    void testRandom() {
        for (int i = 1; i < 21; i++) {
            Program expected = generateProgram(i);
            internalTest(expected.toString(), expected);
        }
    }

    private void internalTest(@NotNull String text, @Nullable Program expected) {
        try {
            System.out.println(SEPARATOR);
            System.out.println(text);
            System.out.println(SEPARATOR);
            Program actual = parser.parse(text);
            if (expected == null) {
                throw new AssertionError("Expected parsing error, but found " + actual + ".");
            }
            assertProgramEquals(expected, actual);
            System.out.println(ANSI_GREEN + "OK: Expression parsed correctly." + ANSI_RESET);
        } catch (ParseException e) {
            if (expected != null) {
                throw new AssertionError("Expected correct expression, but found error.", e);
            }
            System.out.println(ANSI_RED + "OK: Can't parse expression: " + e.getMessage() + ANSI_RESET);
        }
        System.out.println();
    }

    private static Program constructSimpleProgram(String type, String... variables) {
        Description description = new Description(1, new Type(type), Arrays.stream(variables).map(variableName -> {
            int x = variableName.lastIndexOf('*');
            return new Variable(x + 1, variableName.substring(x + 1));
        }).collect(Collectors.toList()));
        return new Program("test", Collections.singletonList(description));
    }

    private static Program generateProgram(int lines) {
        List<Description> descriptions = new ArrayList<>();
        for (int i = 0; i < lines; i++) {
            int n = random.nextInt(32) + 1;
            Type type = new Type(generateWord());
            List<Variable> variables = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                variables.add(new Variable(random.nextInt(32), generateWord()));
            }
            descriptions.add(new Description(i + 1, type, variables));
        }
        return new Program("test", descriptions);
    }

    private static String generateWord() {
        int n = random.nextInt(32) + 1;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(generateChar(false));
        for (int i = 1; i < n; i++) {
            stringBuilder.append(generateChar(true));
        }
        return stringBuilder.toString();
    }

    private static char generateChar(boolean enableDigits) {
        char base;
        int shift = enableDigits ? random.nextInt(63) : random.nextInt(53) + 10;
        if (shift < 10) {
            base = '0';
        } else if (shift < 36) {
            base = 'a';
            shift -= 10;
        } else if (shift < 62) {
            base = 'A';
            shift -= 36;
        } else {
            base = '_';
            shift = 0;
        }
        return (char) (base + shift);
    }

    private static void assertProgramEquals(Program expected, Program actual) {
        assertNotNull(expected);
        assertNotNull(actual);

        List<Description> expectedDescriptions = expected.getDescriptions();
        List<Description> actualDescriptions = actual.getDescriptions();
        assert expectedDescriptions.size() == actualDescriptions.size();

        for (int i = 0; i < expectedDescriptions.size(); i++) {
            Description expectedDescription = expectedDescriptions.get(i);
            Description actualDescription = actualDescriptions.get(i);
            assertDescriptionEquals(expectedDescription, actualDescription);
        }
    }

    private static void assertDescriptionEquals(Description expected, Description actual) {
        assertNotNull(expected);
        assertNotNull(actual);

        Type expectedType = expected.getType();
        Type actualType = actual.getType();
        assertTypeEquals(expectedType, actualType);

        List<Variable> expectedVariables = expected.getVariables();
        List<Variable> actualVariables = actual.getVariables();
        assert expectedVariables.size() == actualVariables.size();

        for (int i = 0; i < expectedVariables.size(); i++) {
            Variable expectedVariale = expectedVariables.get(i);
            Variable actualVariale = actualVariables.get(i);
            assertVariableEquals(expectedVariale, actualVariale);
        }
    }

    private static void assertTypeEquals(Type expected, Type actual) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertEquals(expected.getName(), actual.getName());
    }

    private static void assertVariableEquals(Variable expected, Variable actual) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertEquals(expected.getPointers(), actual.getPointers());
        assertEquals(expected.getName(), actual.getName());
    }

    private static void assertNotNull(Object object) {
        assert object != null;
    }

    private static void assertEquals(Object expected, Object actual) {
        assert expected.equals(actual);
    }
}