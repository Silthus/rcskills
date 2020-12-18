package de.raidcraft.skills;

import de.raidcraft.skills.entities.SkilledPlayer;
import lombok.Value;
import lombok.experimental.Accessors;
import org.bukkit.configuration.ConfigurationSection;

import java.util.function.Supplier;

@FunctionalInterface
public interface Skill {

    /**
     * Load is called with the config of the configured skill after the creation of this skill.
     * <p>Use it to load your skill specific configuration.
     *
     * @param config the config to load the skill with
     */
    default void load(ConfigurationSection config) {}

    /**
     * Applies the effects of this skill to the given player.
     * <p>The apply method is called everytime a player logs on
     * or obtains the skill.
     *
     * @param player the player this skill was applied to
     */
    void apply(SkilledPlayer player);

    /**
     * Removes any effects of the skill from the given player.
     * <p>remove(...) is called everytime the skill becomes inactive and
     * when the player loses access to it.
     *
     * @param player the player this skill was removed from
     */
    default void remove(SkilledPlayer player) {}

    @Value
    @Accessors(fluent = true)
    class Registration<TSkill extends Skill> {

        String type;
        Class<TSkill> skillClass;
        Supplier<TSkill> supplier;
    }
}
