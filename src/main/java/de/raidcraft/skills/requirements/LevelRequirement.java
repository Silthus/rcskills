package de.raidcraft.skills.requirements;

import de.raidcraft.skills.AbstractRequirement;
import de.raidcraft.skills.RequirementInfo;
import de.raidcraft.skills.TestResult;
import de.raidcraft.skills.entities.SkilledPlayer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.silthus.configmapper.ConfigOption;
import org.bukkit.configuration.ConfigurationSection;

import static de.raidcraft.skills.Messages.msg;

@Data
@RequirementInfo("level")
@EqualsAndHashCode(callSuper = true)
public class LevelRequirement extends AbstractRequirement {

    @ConfigOption(required = true)
    private int level = 1;

    @Override
    public String name() {

        return String.format(msg(msgIdentifier("name"), "Level %s"), level);
    }

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
