package scope;

import org.jetbrains.annotations.NotNull;

public final class ScopeUtil {
    public static final String SCOPE_SEPARATOR = "::";
    public static final String DEFAULT_SCOPE = "$";

    public ScopeUtil() {
        throw new UnsupportedOperationException("Can't create an instance of ScopeUtil");
    }

    public static boolean canLevelUp(@NotNull String scope) {
        return scope.contains(SCOPE_SEPARATOR);
    }

    @NotNull
    public static String levelUp(@NotNull String scope) {
        if (!canLevelUp(scope)) {
            throw new IllegalStateException("Can't level up the scope");
        }
        return scope.substring(0, scope.lastIndexOf(SCOPE_SEPARATOR));
    }

    @NotNull
    public static String currentLevel(@NotNull String scope) {
        if (canLevelUp(scope)) {
            return scope.substring(scope.lastIndexOf(SCOPE_SEPARATOR) + 2);
        } else {
            return scope;
        }
    }

    @NotNull
    public static String levelDown(@NotNull String scope, @NotNull String name) {
        return scope + SCOPE_SEPARATOR + name;
    }
}
