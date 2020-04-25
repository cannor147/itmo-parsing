package scope;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class Scope {
    @NotNull
    private String text;

    @NotNull
    private final Map<String, Integer> usages;

    public Scope() {
        this.text = ScopeUtil.DEFAULT_SCOPE;
        this.usages = new HashMap<>();
    }

    public void in(@NotNull String name) {
        usages.putIfAbsent(name, 0);
        int x = usages.get(name);
        usages.put(name, x + 1);
        text = ScopeUtil.levelDown(text, name);
    }

    public void out() {
        text = ScopeUtil.levelUp(text);
    }

    @NotNull
    @Override
    public String toString() {
        return text;
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }

    public boolean isEmpty() {
        return !ScopeUtil.canLevelUp(text);
    }
}
