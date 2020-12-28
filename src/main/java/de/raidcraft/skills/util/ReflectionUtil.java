package de.raidcraft.skills.util;

import lombok.NonNull;

import java.util.Map;
import java.util.Optional;

public final class ReflectionUtil {

    /**
     * Takes the given map and target and tries to extract the nearest possible type match of the target.
     *
     * @param targetClass the class of the target to find a match for
     * @param map       map to find match in
     * @param <TTarget> target type
     * @param <TResult> result type of the map
     * @return extracted map value if the target type matched and was found
     */
    public static <TTarget, TResult> Optional<TResult> getEntryForTarget(@NonNull Class<TTarget> targetClass, @NonNull Map<Class<?>, TResult> map) {

        if (map.containsKey(targetClass)) {
            return Optional.ofNullable(map.get(targetClass));
        }

        Class<?> currentTargetClass = null;
        TResult result = null;
        for (Map.Entry<Class<?>, TResult> entry : map.entrySet()) {
            if (entry.getKey().isAssignableFrom(targetClass)) {
                // pick the nearest possible result we can find
                if (currentTargetClass == null || currentTargetClass.isAssignableFrom(entry.getKey())) {
                    currentTargetClass = entry.getKey();
                    result = entry.getValue();
                }
            }
        }

        return Optional.ofNullable(result);
    }
}
