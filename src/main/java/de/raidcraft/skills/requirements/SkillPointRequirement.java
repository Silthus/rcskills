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
@RequirementInfo("skillpoints")
@EqualsAndHashCode(callSuper = true)
public class SkillPointRequirement extends AbstractRequirement {

    @ConfigOption(required = true)
    private int skillpoints = 0;

    @Override
    public String name() {

        return String.format(msg(msgIdentifier("name"), "%s Skillpunkt(e)"), skillpoints);
    }

    @Override
    public String description() {

        return String.format(msg(msgIdentifier("description"), "Du benötigst mindestens %s Skillpunkt(e) um diesen Skill freizuschalten."), skillpoints);
    }

    @Override
    protected void loadConfig(ConfigurationSection config) {

        this.skillpoints = config.getInt("skillpoints", 0);
    }

    @Override
    public TestResult test(@NonNull SkilledPlayer target) {

        return TestResult.of(target.skillPoints() >= skillpoints, "Du benötigst " + skillpoints + " Skillpunkte um diesen Skill freizuschalten.");
    }
}
