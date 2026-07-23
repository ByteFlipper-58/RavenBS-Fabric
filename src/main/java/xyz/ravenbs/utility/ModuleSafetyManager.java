package xyz.ravenbs.utility;

import net.minecraft.client.option.KeyBinding;
import xyz.ravenbs.module.Module;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Tracks keys held by modules so they cannot remain pressed after a failure or disconnect. */
public final class ModuleSafetyManager {
    private static final Map<KeyBinding, Set<Module>> forcedKeys = new HashMap<>();

    private ModuleSafetyManager() {
    }

    public static synchronized void setKeyPressed(Module owner, KeyBinding key, boolean pressed) {
        if (owner == null || key == null) {
            return;
        }

        Set<Module> owners = forcedKeys.computeIfAbsent(key, ignored -> new HashSet<>());
        if (pressed) {
            owners.add(owner);
        } else {
            owners.remove(owner);
        }

        if (owners.isEmpty()) {
            forcedKeys.remove(key);
            key.setPressed(false);
        } else {
            key.setPressed(true);
        }
    }

    public static synchronized void releaseModule(Module owner) {
        if (owner == null) {
            return;
        }

        for (KeyBinding key : Set.copyOf(forcedKeys.keySet())) {
            Set<Module> owners = forcedKeys.get(key);
            owners.remove(owner);
            if (owners.isEmpty()) {
                forcedKeys.remove(key);
                key.setPressed(false);
            }
        }
    }

    public static synchronized void resetAll() {
        for (KeyBinding key : forcedKeys.keySet()) {
            key.setPressed(false);
        }
        forcedKeys.clear();
    }
}
