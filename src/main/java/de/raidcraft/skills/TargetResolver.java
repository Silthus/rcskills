package de.raidcraft.skills;

import java.util.Optional;

/**
 * Resolves the given target type based on the provided execution context.
 * <p>This could be a block or entity the player is looking at or even custom types
 * based on other plugins that are installed on the server, e.g. the town the player is member of.
 * <p>All target resolvers must be registered with the {@link TargetManager}.
 *
 * @param <TTarget> the type of the target that will be resolved
 */
@FunctionalInterface
public interface TargetResolver<TTarget> {

    /**
     * Resolves the given target type with the given execution context.
     * <p>An empty optional will be returned for all cases where the resolution fails.
     *
     * @param context the context that called the resolution of the target type
     * @return the resolved target or an empty optional
     */
    Optional<TTarget> resolve(ExecutionContext context);
}
