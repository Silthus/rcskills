package net.silthus.skills;

import lombok.Value;
import lombok.experimental.Accessors;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.function.Supplier;

public interface Skill {

    String identifier();

    String name();

    String[] description();

    Collection<Requirement> requirements();

    Collection<String> permissions();

    default TestResult test(Player player) {

        return requirements().stream()
                .map(requirement -> requirement.test(player))
                .reduce(TestResult::merge)
                .orElse(TestResult.ofSuccess());
    }

    default void apply(Player player) {

        for (String permission : permissions()) {
            player.addAttachment(SkillsPlugin.getInstance(), permission, true);
        }
    }

    @Value
    @Accessors(fluent = true)
    class Registration {

        String identifier;
        Class<? extends Skill> skillClass;
        Supplier<Skill> supplier;
    }
}
