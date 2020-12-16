package net.silthus.skills.requirements;

import lombok.NonNull;
import net.silthus.configmapper.ConfigOption;
import net.silthus.skills.AbstractRequirement;
import net.silthus.skills.RequirementType;
import net.silthus.skills.TestResult;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.configuration.ConfigurationSection;

@RequirementType("level")
public class LevelRequirement extends AbstractRequirement {

    @ConfigOption(required = true)
    private int level = 1;

    @Override
    protected void loadConfig(ConfigurationSection config) {

        level = config.getInt("level", 1);
    }

    @Override
    public TestResult test(@NonNull SkilledPlayer target) {

        return TestResult.of(target.level().getLevel() >= level,
                "Du benötigst mindestens Level " + level + " um diesen Skill freizuschalten.");
    }
}
