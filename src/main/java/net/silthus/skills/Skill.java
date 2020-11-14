package net.silthus.skills;

import lombok.Value;
import lombok.experimental.Accessors;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Collection;
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
