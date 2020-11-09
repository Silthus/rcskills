package net.silthus.skills;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.function.Supplier;

public interface Requirement {

    String identifier();

    String name();

    String description();

    default Requirement load(ConfigurationSection config) {

        return this;
    }

    TestResult test(@NonNull Player target);

    @Value
    @Accessors(fluent = true)
    class Registration {
        String identifier;
        Class<? extends Requirement> requirementClass;
        Supplier<Requirement> supplier;
    }
}
