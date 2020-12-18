package de.raidcraft.skills;

import de.raidcraft.skills.entities.SkilledPlayer;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import org.bukkit.configuration.ConfigurationSection;

import java.util.function.Supplier;

public interface Requirement {

    String type();

    String name();

    String description();

    default boolean visible() {
        return !hidden();
    }

    boolean hidden();

    default Requirement load(ConfigurationSection config) {

        return this;
    }

    TestResult test(@NonNull SkilledPlayer target);

    @Value
    @Accessors(fluent = true)
    class Registration<TRequirement extends Requirement> {
        String identifier;
        Class<TRequirement> requirementClass;
        Supplier<TRequirement> supplier;
    }
}
