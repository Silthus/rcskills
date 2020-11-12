package net.silthus.skills;

import lombok.*;
import lombok.experimental.Accessors;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(of = "identifier")
public abstract class AbstractSkill implements Skill {

    private String identifier = "undefined";
    private String name = identifier;
    private String[] description = new String[0];
    private List<Requirement> requirements = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();

    @Override
    public final Skill load(ConfigurationSection config) {
        this.identifier = config.getString("id", identifier);
        this.name = config.getString("name", identifier);
        this.description = config.getString("description", "").split("\\|");
        this.permissions = config.getStringList("permissions");
        this.requirements = SkillManager.instance().loadRequirements(config.getConfigurationSection("requirements"));

        return this;
    }

    protected abstract void loadSkill(ConfigurationSection config);
}
