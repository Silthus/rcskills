package de.raidcraft.skills;

import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * The ExecutionContext holds additional information about the execution of the skill.
 * <p>Use it to get the source of the execution, the player and more.
 * <p>A new execution context is created everytime a skill is executed.
 */
public interface ExecutionContext {

    /**
     * Creates a new execution context based of the given skill context.
     *
     * @param context the skill context of this execution
     * @return the new execution context
     */
    static ExecutionContext of(SkillContext context) {

        return new DefaultExecutionContext(context);
    }

    /**
     * @return the player that holds the skill and executed it
     */
    default Player player() {

        return source().player().orElse(null);
    }

    /**
     * Tries to find a valid target for the execution of the skill.
     * <p>You can pass in any class that has a valid {@link TargetResolver} registered.
     * By default this can be any of the following:
     * <ul>
     *     <li>{@link Player} - target the player the executing player is looking at
     *     <li>{@link Block} - target the next solid block the player is looking at
     *     <li>{@link LivingEntity} - target the entity the player is looking at
     *     <br>You can use any sub class of LivingEntity to target specific entities.
     * </ul>
     * <p>An empty optional will be returned if the given target type does not exist,
     * has no registered resolver or if nothing is targeted.
     *
     * @param targetClass the target type that should be resolved
     * @param <TTarget> the type of the target
     * @return the given target or an empty optional
     */
    <TTarget> Optional<TTarget> target(Class<TTarget> targetClass);

    /**
     * The source (skill context) of the execution.
     *
     * @return the skill context of the execution
     */
    SkillContext source();

    /**
     * @return the config of this execution
     */
    ExecutionConfig config();

    /**
     * Gets the config value of this skill execution.
     *
     * @param key the key of the configured value
     * @param defaultValue the default value to use if the config option is not set
     * @param <TValue> the type of the value
     * @return the given config value or the default if it is not set
     */
    default <TValue> TValue config(String key, TValue defaultValue) {

        return config().get(key, defaultValue);
    }

    /**
     * @see ExecutionResult#success(ExecutionContext)
     */
    default ExecutionResult success() {
        return ExecutionResult.success(this);
    }

    /**
     * @see ExecutionResult#failure(ExecutionContext, String...)
     */
    default ExecutionResult failure(String... errors) {
        return ExecutionResult.failure(this, errors);
    }

    /**
     * @see ExecutionResult#of(ExecutionContext, boolean, String...)
     */
    default ExecutionResult resultOf(boolean success, String... errors) {
        return ExecutionResult.of(this, success, errors);
    }
}
