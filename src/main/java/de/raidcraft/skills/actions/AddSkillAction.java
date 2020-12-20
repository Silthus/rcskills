package de.raidcraft.skills.actions;

import com.google.common.base.Strings;
import de.raidcraft.skills.TestResult;
import de.raidcraft.skills.entities.ConfiguredSkill;
import de.raidcraft.skills.entities.PlayerSkill;
import de.raidcraft.skills.entities.SkilledPlayer;
import de.raidcraft.skills.events.AddPlayerSkillEvent;
import io.ebean.annotation.Transactional;
import lombok.Value;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;

@Value
@Accessors(fluent = true)
public class AddSkillAction {

    SkilledPlayer player;
    ConfiguredSkill skill;

    @Transactional
    public Result execute(boolean bypassChecks) {

        if (player.hasSkill(skill)) {
            return new Result(this, player.name() + " hat bereits den Skill: " + skill.alias());
        }

        AddPlayerSkillEvent event = new AddPlayerSkillEvent(this, bypassChecks);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return new Result(this, "Das Hinzufügen des Skills " + skill.alias() + " wurde durch ein Plugin verhindert.");
        }

        TestResult testResult = skill.testRequirements(player);
        if (!testResult.success() && !event.isBypassChecks()) {
            return new Result(this, testResult, "Die Vorraussetzungen für den Skill " + skill.alias() + " sind nicht erfüllt.");
        }

        PlayerSkill playerSkill = PlayerSkill.getOrCreate(player, skill);
        playerSkill.unlock();

        player.save();
        return new Result(this, playerSkill, testResult);
    }

    @Value
    @Accessors(fluent = true)
    public static class Result {

        AddSkillAction action;
        PlayerSkill playerSkill;
        TestResult testResult;
        String error;

        public Result(AddSkillAction action, TestResult testResult, String error) {
            this.action = action;
            this.testResult = testResult;
            this.error = error;
            playerSkill = PlayerSkill.getOrCreate(action.player, action.skill);
        }

        public Result(AddSkillAction action, PlayerSkill playerSkill, TestResult testResult) {
            this.action = action;
            this.playerSkill = playerSkill;
            this.testResult = testResult;
            this.error = null;
        }

        public Result(AddSkillAction action, String error) {
            this.action = action;
            this.testResult = null;
            this.error = error;
            playerSkill = PlayerSkill.getOrCreate(action.player, action.skill);
        }

        public boolean success() {
            return Strings.isNullOrEmpty(error);
        }
    }
}
