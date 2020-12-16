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
@RequirementInfo("skillpoint")
@EqualsAndHashCode(callSuper = true)
public class SkillPointRequirement extends AbstractRequirement {

    @ConfigOption(required = true)
    private int skillpoints = 0;

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
