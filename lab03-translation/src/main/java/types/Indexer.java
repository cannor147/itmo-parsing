package types;

import context.ParseContext;
import context.Stage;
import exception.*;
import lang.mutability.Mutability;
import lang.object.NevelFunction;
import lang.object.NevelVariable;
import lang.type.PrimitiveType;
import lang.type.StringType;
import lang.type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scope.Scope;
import scope.ScopeUtil;

import java.util.*;

@SuppressWarnings("unused")
public class Indexer {
    public static final List<Type> GENERAL_TYPES = new ArrayList<>();

    static {
        GENERAL_TYPES.add(PrimitiveType.BOOL);
        GENERAL_TYPES.add(PrimitiveType.BYTE);
        GENERAL_TYPES.add(PrimitiveType.SHORT);
        GENERAL_TYPES.add(PrimitiveType.INT);
        GENERAL_TYPES.add(PrimitiveType.LONG);
        GENERAL_TYPES.add(PrimitiveType.FLOAT);
        GENERAL_TYPES.add(PrimitiveType.DOUBLE);
        GENERAL_TYPES.add(PrimitiveType.CHAR);
        GENERAL_TYPES.add(StringType.getInstance());
    }

    @NotNull
    private final Map<String, List<NevelFunction>> functionIndex;

    @NotNull
    private final Map<String, NevelVariable> variableIndex;

    private void addFunction(@NotNull String name, @NotNull Type resultType, Type... arguments) {
        NevelFunction function = new NevelFunction(name, Arrays.asList(arguments), resultType);
        functionIndex.put("$::" + name, Collections.singletonList(function));
    }

    public Indexer() {
        this.functionIndex = new HashMap<>();
        this.variableIndex = new HashMap<>();

        for (Type generalType : GENERAL_TYPES) {
            addFunction("print", PrimitiveType.VOID, generalType);
            addFunction("println", PrimitiveType.VOID, generalType);
        }
        addFunction("length", PrimitiveType.INT, StringType.getInstance());
    }

    public void indexVariable(@NotNull String name, @Nullable Type type, @Nullable Mutability mutability, @NotNull ParseContext parseContext) {
        Scope currentScope = parseContext.getScope();
        if ((parseContext.getStage() == Stage.INDEXATION && currentScope.isEmpty()) || (parseContext.getStage() == Stage.CONSTRUCTION && !currentScope.isEmpty())) {
            if (type == null) {
                throw new ParseException("Can't call indexVariable with null type", parseContext);
            } else if (mutability == null) {
                throw new ParseException("Can't call indexVariable with null mutability", parseContext);
            }

            NevelVariable variable = new NevelVariable(name, type, mutability);
            String key = ScopeUtil.levelDown(currentScope.toString(), name);

            NevelVariable v = internalFindVariable(key);
            if (v != null) {
                throw new AlreadyDefinedVariableException(name, parseContext);
            } else {
                variableIndex.put(key, variable);
            }
        }
    }
    public void indexFunction(@NotNull String name, @NotNull List<Type> argumentTypes, @Nullable Type resultType, @NotNull ParseContext parseContext) {
        Scope currentScope = parseContext.getScope();
        if (parseContext.getStage() == Stage.INDEXATION) {
            if (argumentTypes.stream().anyMatch(Objects::isNull) || resultType == null) {
                throw new ParseException("Can't call indexFunction with null types", parseContext);
            }

            NevelFunction function = new NevelFunction(name, argumentTypes, resultType);
            String key = ScopeUtil.levelDown(currentScope.toString(), name);

            NevelFunction v = internalFindFunction(key, argumentTypes);
            if (v != null) {
                throw new AlreadyDefinedFunctionException(name, parseContext);
            } else {
                functionIndex.get(key).add(function);
            }
        }
    }

    @Nullable
    public NevelVariable findVariable(@NotNull String name, @NotNull ParseContext parseContext) {
        if (parseContext.getStage() == Stage.CONSTRUCTION) {
            String scope = parseContext.getScope().toString();
            boolean lastTry = false;
            while (ScopeUtil.canLevelUp(scope) || lastTry) {
                NevelVariable variable = internalFindVariable(ScopeUtil.levelDown(scope, name));
                if (variable != null) {
                    return variable;
                } else if (lastTry) {
                    break;
                } else {
                    scope = ScopeUtil.levelUp(scope);
                    if (!ScopeUtil.canLevelUp(scope)) {
                        lastTry = true;
                    }
                }
            }
            throw new CannotResolveVariableException(name, parseContext);
        } else {
            return null;
        }
    }

    @Nullable
    public NevelFunction findFunction(@NotNull String name, @NotNull List<Type> argumentTypes, @NotNull ParseContext parseContext) {
        if (parseContext.getStage() == Stage.CONSTRUCTION) {
            String scope = parseContext.getScope().toString();
            boolean lastTry = false;
            while (ScopeUtil.canLevelUp(scope) || lastTry) {
                NevelFunction function = internalFindFunction(ScopeUtil.levelDown(scope, name), argumentTypes);
                if (function != null) {
                    return function;
                } else if (lastTry) {
                    break;
                } else {
                    scope = ScopeUtil.levelUp(scope);
                    if (!ScopeUtil.canLevelUp(scope)) {
                        lastTry = true;
                    }
                }
            }
            throw new CannotResolveFunctionException(name, parseContext);
        } else {
            return null;
        }
    }

    @Nullable
    private NevelVariable internalFindVariable(@NotNull String key) {
        return variableIndex.get(key);
    }

    @Nullable
    private NevelFunction internalFindFunction(@NotNull String key, @NotNull List<Type> argumentTypes) {
        List<NevelFunction> functions = functionIndex.computeIfAbsent(key, s -> new ArrayList<>());
        for (NevelFunction f : functions) {
            int k = f.getArgumentTypes().size();
            if (k != argumentTypes.size()) {
                continue;
            }

            boolean c = true;
            for (int i = 0; i < k; i++) {
                Type t = f.getArgumentTypes().get(i);
                Type argumentType = argumentTypes.get(i);
                if (!t.equals(argumentType)) {
                    c = false;
                    break;
                }
            }

            if (c) {
                return f;
            }
        }
        return null;
    }
}
