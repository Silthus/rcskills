package net.silthus.skills;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.function.Supplier;

public interface Requirement {

    String type();

    String name();

    String description();

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
