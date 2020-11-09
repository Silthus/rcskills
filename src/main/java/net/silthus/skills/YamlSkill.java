package net.silthus.skills;

import lombok.Value;
import lombok.experimental.Accessors;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

@Value
@Accessors(fluent = true)
public class YamlSkill implements Skill {

    String identifier;
    String name;
    String[] description;
    List<Requirement> requirements;
    List<String> permissions;

    public YamlSkill(String identifier, ConfigurationSection config) {

        this.identifier = config.getString("id", identifier);
        this.name = config.getString("name", identifier);
        this.description = config.getString("description", "").split("\\|");
        this.permissions = config.getStringList("permissions");
        this.requirements = SkillManager.instance().loadRequirements(config.getConfigurationSection("requirements"));
    }
}
