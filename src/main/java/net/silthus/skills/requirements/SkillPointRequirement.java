package net.silthus.skills.requirements;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.silthus.configmapper.ConfigOption;
import net.silthus.skills.AbstractRequirement;
import net.silthus.skills.RequirementType;
import net.silthus.skills.TestResult;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.configuration.ConfigurationSection;

@Data
@RequirementType("skillpoint")
@EqualsAndHashCode(callSuper = true)
public class SkillPointRequirement extends AbstractRequirement {

    @ConfigOption(required = true)
    private int skillpoints = 0;

    @Override
    protected void loadConfig(ConfigurationSection config) {

        this.skillpoints = config.getInt("skillpoints", 0);
    }

    @Override
    public TestResult test(@NonNull SkilledPlayer target) {

        return TestResult.of(target.skillPoints() >= skillpoints, "Du benötigst " + skillpoints + " Skillpunkte um diesen Skill freizuschalten.");
    }
}
