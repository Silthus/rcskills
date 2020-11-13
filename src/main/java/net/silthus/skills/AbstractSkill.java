package net.silthus.skills;

import lombok.*;
import lombok.experimental.Accessors;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(of = "identifier")
public abstract class AbstractSkill implements Skill {

    private String identifier = "undefined";
    private String name = identifier;
    private String description;
    private final List<Requirement> requirements = new ArrayList<>();

    @Override
    public final Skill load(ConfigurationSection config) {
        this.identifier = config.getString("id", identifier);
        this.name = config.getString("name", identifier);
        this.description = config.getString("description", "");

        return this;
    }

    @Override
    public void addRequirement(@NonNull Requirement requirement) {
        this.requirements.add(requirement);
    }

    @Override
    public void addRequirements(Collection<Requirement> requirements) {
        this.requirements.addAll(requirements);
    }

    protected abstract void loadSkill(ConfigurationSection config);
}
