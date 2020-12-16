package net.silthus.skills.requirements;

import lombok.NonNull;
import net.silthus.skills.AbstractRequirement;
import net.silthus.skills.RequirementType;
import net.silthus.skills.TestResult;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.configuration.ConfigurationSection;

import static net.silthus.skills.Messages.msg;

@RequirementType("skill")
public class SkillRequirement extends AbstractRequirement {

    private String skill;

    @Override
    public String description() {

        return String.format(msg(msgIdentifier("description"), "Requires the %1$s skill to unlock this skill."), skill);
    }

    @Override
    public void loadConfig(ConfigurationSection config) {

        this.skill = config.getString("skill");
    }

    @Override
    public TestResult test(@NonNull SkilledPlayer player) {

        return TestResult.of(player.hasSkill(skill), String.format(msg(msgIdentifier("error"), "You require the %1$s skill to unlock this skill."), skill));
    }
}
