package net.silthus.skills;

import lombok.Data;
import lombok.experimental.Accessors;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Accessors(fluent = true)
public class ConfiguredSkill implements Skill {

    private final Skill skill;
    private final List<Requirement> requirements = new ArrayList<>();

    private String identifier;
    private String name;
    private String description;

    public ConfiguredSkill(Skill skill) {
        this.skill = skill;
    }

    @Override
    public void load(ConfigurationSection config) {
        this.identifier = config.getString("id");
        this.name = config.getString("name", identifier());
        this.description = config.getString("description");
        if (config.getConfigurationSection("with") != null) {
            this.skill.load(config.getConfigurationSection("with"));
        } else {
            this.skill.load(config.createSection("with"));
        }
    }

    @Override
    public void apply(SkilledPlayer player) {
        this.skill.apply(player);
    }

    @Override
    public void remove(SkilledPlayer player) {
        this.skill.remove(player);
    }

    public void addRequirement(Requirement... requirements) {
        this.requirements.addAll(Arrays.asList(requirements));
    }

    public TestResult test(SkilledPlayer player) {

        return requirements().stream()
                .map(requirement -> requirement.test(player))
                .reduce(TestResult::merge)
                .orElse(TestResult.ofSuccess());
    }
}
