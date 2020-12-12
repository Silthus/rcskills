package net.silthus.skills;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

import static net.silthus.skills.Messages.msg;

@Data
@Accessors(fluent = true)
public abstract class AbstractRequirement implements Requirement {

    private final String type;
    private String name;
    private String description;
    private boolean negate = false;

    public AbstractRequirement() {
        if (!getClass().isAnnotationPresent(RequirementType.class)) {
            throw new RuntimeException("Cannot instantiate a requirement without a type identifier. " +
                    "Use the @RequirementType annotation or call super(...)");
        }
        this.type = getClass().getAnnotation(RequirementType.class).value().toLowerCase();
    }

    @Override
    public final Requirement load(ConfigurationSection config) {

        this.name = config.getString("name", msg(msgIdentifier("name"), type));
        this.description = config.getString("description", msg(msgIdentifier("description")));
        this.negate = config.getBoolean("negate", false);

        loadConfig(Objects.requireNonNullElseGet(
                config.getConfigurationSection("with"),
                () -> config.createSection("with")));

        return this;
    }

    protected abstract void loadConfig(ConfigurationSection config);

    protected final String msgIdentifier(String key) {

        return "requirements." + type() + "." + key;
    }
}
