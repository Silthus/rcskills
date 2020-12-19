package de.raidcraft.skills;

import de.raidcraft.skills.entities.PlayerSkill;
import lombok.Value;
import lombok.experimental.Accessors;
import org.bukkit.configuration.ConfigurationSection;

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

    @Value
    @Accessors(fluent = true)
    class Registration<TSkill extends Skill> {

        Class<TSkill> skillClass;
        SkillInfo info;
        Function<SkillContext, TSkill> supplier;

        public String type() {

            return info().value();
        }
    }
}
