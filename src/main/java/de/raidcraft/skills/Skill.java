package de.raidcraft.skills;

import de.raidcraft.skills.entities.DataStore;
import de.raidcraft.skills.entities.PlayerSkill;
import lombok.Value;
import lombok.experimental.Accessors;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;

public interface Skill {

    /**
     * The context of the skill holds additional information
     * about the executing player and provides methods to access
     * the data store and other convenience methods.
     *
     * @return the executing context of this skill
     */
    SkillContext context();

    /**
     * @return the unique id of the player skill
     */
    default UUID id() {

        return context().playerSkill().id();
    }

    /**
     * @return the friendly name of the skill
     */
    default String name() {

        return context().configuredSkill().name();
    }

    /**
     * @return the alias of the skill
     */
    default String alias() {

        return context().configuredSkill().alias();
    }

    /**
     * @return the datastore of the skill for storing persistent data
     * @see SkillContext#store()
     */
    default DataStore store() {

        return context().store();
    }

    /**
     * Checks if the skill is on cooldown.
     * <p>Will return false if the skill has no configured cooldown.
     *
     * @return true if the skill is on cooldown and cannot be excuted
     */
    default boolean isOnCooldown() {

        return context().isOnCooldown();
    }

    /**
     * @return the remaining cooldown of the skill in milliseconds
     *         zero or less if no cooldown is configured or the cooldown is over
     * @see SkillContext#getRemainingCooldown()
     */
    default long getRemainingCooldown() {

        return context().getRemainingCooldown();
    }

    /**
     * Gets the time the skill was last used.
     * <p>{@link Instant#EPOCH} will be returned if the skill was never used.
     *
     * @return the time the skill was last used
     * @see PlayerSkill#lastUsed()
     */
    default Instant lastUsed() {

        return context().playerSkill().lastUsed();
    }

    /**
     * Sets the time the skill was last used to the given Instant.
     *
     * @param instant the instant the skill was last used
     * @return this skill instance
     * @see PlayerSkill#lastUsed(Instant)
     */
    default Skill lastUsed(Instant instant) {

        context().playerSkill().lastUsed(instant).save();
        return this;
    }

    /**
     * Load is called with the config of the configured skill after the creation of this skill.
     * <p>Use it to load your skill specific configuration.
     * <p>You can use the {@link net.silthus.configmapper.ConfigOption} annotations as an alternative.
     *
     * @param config the config to load the skill with
     */
    default void load(ConfigurationSection config) {}

    /**
     * Applies the effects of this skill to the given player.
     * <p>The apply method is called everytime a player logs on
     * or obtains the skill.
     */
    default void apply() {}

    /**
     * Removes any effects of the skill from the given player.
     * <p>remove(...) is called everytime the skill becomes inactive and
     * when the player loses access to it, e.g. when logging out.
     */
    default void remove() {}

    /**
     * Checks if this skill is applicable to the given player.
     * <p>This will check if the given player is the same as the skilled player.
     *
     * @param player the player to check. can be null.
     * @return false if the player is null or not the same as the skilled player of this skill
     */
    default boolean applicable(OfflinePlayer player) {

        return context().applicable(player);
    }

    /**
     * This is a shortcut to the {@link #applicable(OfflinePlayer)} method
     * inverting the result.
     *
     * @param player the player to check. can be null.
     * @return true if the player is null or not the player of this skill
     */
    default boolean notApplicable(OfflinePlayer player) {

        return context().notApplicable(player);
    }

    @Value
    @Accessors(fluent = true)
    class Registration<TSkill extends Skill> {

        Class<TSkill> skillClass;
        SkillInfo info;
        Function<SkillContext, TSkill> supplier;

        public String type() {

            return info().value();
        }

        public boolean executableSkill() {

            return Executable.class.isAssignableFrom(skillClass);
        }
    }
}
