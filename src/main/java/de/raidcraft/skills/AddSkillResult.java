package de.raidcraft.skills;

import de.raidcraft.skills.entities.PlayerSkill;
import de.raidcraft.skills.entities.SkilledPlayer;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
public class AddSkillResult {

    Skill skill;
    SkilledPlayer player;
    PlayerSkill playerSkill;
    TestResult testResult;
    boolean success;
    boolean bypassedChecks;
    String errorMessage;

    public AddSkillResult(Skill skill, SkilledPlayer player, PlayerSkill playerSkill, TestResult testResult, boolean success, boolean bypassedChecks, String errorMessage) {

        this.skill = skill;
        this.player = player;
        this.playerSkill = playerSkill;
        this.testResult = testResult;
        this.success = success;
        this.bypassedChecks = bypassedChecks;
        this.errorMessage = errorMessage;
    }

    public AddSkillResult(Skill skill, SkilledPlayer player, PlayerSkill playerSkill, TestResult testResult, boolean success, boolean bypassedChecks) {

        this.skill = skill;
        this.player = player;
        this.playerSkill = playerSkill;
        this.testResult = testResult;
        this.success = success;
        this.bypassedChecks = bypassedChecks;
        this.errorMessage = null;
    }
}
