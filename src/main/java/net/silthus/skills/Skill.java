package net.silthus.skills;

import lombok.Value;
import lombok.experimental.Accessors;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.function.Supplier;

public interface Skill {

    String identifier();

    String name();

    String description();

    boolean unlocked();

    boolean active();

    Collection<Requirement> requirements();

    void addRequirement(Requirement requirement);

    void addRequirements(Collection<Requirement> requirements);

    Skill load(ConfigurationSection config);

    void unlock(Player player);

    default TestResult test(Player player) {

        return requirements().stream()
                .map(requirement -> requirement.test(player))
                .reduce(TestResult::merge)
                .orElse(TestResult.ofSuccess());
    }

    /**
     * Applies the effects of this skill to the given player.
     * <p>The apply method is called everytime a player logs on
     * or obtains the skill.
     *
     * @param player the player this skill was applied to
     */
    void apply(Player player);

    /**
     * Removes any effects of the skill from the given player.
     * @param player
     */
    void remove(Player player);

    @Value
    @Accessors(fluent = true)
    class Registration<TSkill extends Skill> {

        String identifier;
        Class<TSkill> skillClass;
        Supplier<TSkill> supplier;
    }
}
