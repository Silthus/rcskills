package de.raidcraft.skills.requirements;

import com.google.common.base.Strings;
import de.raidcraft.skills.AbstractRequirement;
import de.raidcraft.skills.RequirementInfo;
import de.raidcraft.skills.TestResult;
import de.raidcraft.skills.entities.ConfiguredSkill;
import de.raidcraft.skills.entities.SkilledPlayer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

import static de.raidcraft.skills.Messages.msg;

@Data
@EqualsAndHashCode(callSuper = true)
@RequirementInfo("skill")
@Accessors(fluent = true)
@Log(topic = "RCSkills")
public class SkillRequirement extends AbstractRequirement {

    private ConfiguredSkill skill;

    @Override
    public String name() {

        return String.format(msg(msgIdentifier("name"), "%s Skill"), skill);
    }

    @Override
    public String description() {

        return String.format(msg(msgIdentifier("description"), "Requires the %s skill to unlock this skill."), skill);
    }

    @Override
    public void loadConfig(ConfigurationSection config) {

        String skill = config.getString("skill");
        if (Strings.isNullOrEmpty(skill)) return;

        try {
            UUID id = UUID.fromString(skill);
            this.skill = ConfiguredSkill.find.byId(id);
        } catch (IllegalArgumentException e) {
            this.skill = ConfiguredSkill.findByAliasOrName(skill).orElse(null);
        }

        if (this.skill == null) {
            log.warning("unable to find skill " + skill + " for skill requirement");
        }
    }

    @Override
    public TestResult test(@NonNull SkilledPlayer player) {

        if (skill == null) return TestResult.ofError("unknown skill requirement");

        return TestResult.of(player.hasSkill(skill), String.format(msg(msgIdentifier("error"), "You require the %1$s skill to unlock this skill."), skill.alias()));
    }
}
