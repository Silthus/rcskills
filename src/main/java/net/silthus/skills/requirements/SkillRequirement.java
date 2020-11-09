package net.silthus.skills.requirements;

import lombok.NonNull;
import net.silthus.skills.AbstractRequirement;
import net.silthus.skills.Requirement;
import net.silthus.skills.SkillManager;
import net.silthus.skills.entities.SkilledPlayer;
import net.silthus.skills.TestResult;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import static net.silthus.skills.Messages.msg;

public class SkillRequirement extends AbstractRequirement {

    private final SkillManager skillManager;
    private String skill;

    public SkillRequirement(SkillManager skillManager) {

        super("skill");
        this.skillManager = skillManager;
    }

    @Override
    public String description() {

        return String.format(msg(msgIdentifier("description"), "Requires the %1$s skill to unlock this skill."), skill);
    }

    @Override
    public Requirement load(ConfigurationSection config) {

        this.skill = config.getString("skill");
        return this;
    }

    @Override
    public TestResult test(@NonNull Player player) {

        SkilledPlayer skilledPlayer = skillManager.getPlayer(player);
        return TestResult.of(skilledPlayer.hasSkill(skill), String.format(msg(msgIdentifier("error"), "You require the %1$s skill to unlock this skill."), skill));
    }
}
