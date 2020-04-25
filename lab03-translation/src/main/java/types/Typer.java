package types;

import context.Stage;
import exception.CannotAssignToConstVariableException;
import context.ParseContext;
import exception.ParseException;
import exception.TypeNotSubtypeException;
import exception.TypesNotEqualsException;
import lang.mutability.Mutability;
import lang.type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scope.Scope;
import scope.ScopeUtil;

public class Typer {
    public void ensureEquals(@Nullable Type expected, @Nullable Type actual, @NotNull ParseContext parseContext) {
        if (parseContext.getStage() == Stage.CONSTRUCTION) {
            if (expected == null || actual == null) {
                throw new ParseException("Can't call ensureEquals with null types", parseContext);
            } else if (!actual.equals(expected)) {
                throw new TypesNotEqualsException(expected, actual, parseContext);
            }
        }
    }

    public void ensureSubtype(@Nullable Type expected, @Nullable Type actual, @NotNull ParseContext parseContext) {
        if (parseContext.getStage() == Stage.CONSTRUCTION) {
            if (expected == null || actual == null) {
                throw new ParseException("Can't call ensureSubtype with null types", parseContext);
            } else if (!actual.subtypeOf(expected)) {
                throw new TypeNotSubtypeException(expected, actual, parseContext);
            }
        }
    }

    public void ensureMutable(@NotNull String name, @Nullable Mutability actual, @NotNull ParseContext parseContext) {
        if (parseContext.getStage() == Stage.CONSTRUCTION) {
            if (actual == null) {
                throw new ParseException("Can't call ensureMutable with null mutability", parseContext);
            } else if (actual != Mutability.MUTABLE) {
                throw new CannotAssignToConstVariableException(name, parseContext);
            }
        }
    }

    public void ensureInsideFunction(@NotNull String name, @NotNull ParseContext parseContext) {
        Scope currentScope = parseContext.getScope();
        String scope = currentScope.toString();
        while (ScopeUtil.canLevelUp(scope)) {
            String node = ScopeUtil.currentLevel(scope);
            scope = ScopeUtil.levelUp(scope);
            if (node.startsWith("@FUNCTION")) {
                return;
            }
        }
        throw new ParseException(name + " is allowed only inside a function", parseContext);
    }

    public void ensureInsideCycle(@NotNull String name, @NotNull ParseContext parseContext) {
        Scope currentScope = parseContext.getScope();
        String scope = currentScope.toString();
        while (ScopeUtil.canLevelUp(scope)) {
            String node = ScopeUtil.currentLevel(scope);
            scope = ScopeUtil.levelUp(scope);
            if (node.startsWith("@WHILE")) {
                return;
            }
        }
        throw new ParseException(name + " is allowed only inside a cycle", parseContext);
    }
}
