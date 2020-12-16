package net.silthus.skills.requirements;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.silthus.configmapper.ConfigOption;
import net.silthus.skills.AbstractRequirement;
import net.silthus.skills.RequirementInfo;
import net.silthus.skills.TestResult;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.configuration.ConfigurationSection;

import static net.silthus.skills.Messages.msg;

@Data
@RequirementInfo("level")
@EqualsAndHashCode(callSuper = true)
public class LevelRequirement extends AbstractRequirement {

    @ConfigOption(required = true)
    private int level = 1;

    @Override
    public String description() {

        return String.format(msg(msgIdentifier("description"), "Du musst mind. Level %s sein um diesen Skill zu freizuschalten."), level);
    }

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
