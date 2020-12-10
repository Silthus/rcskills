package net.silthus.skills.actions;

import com.google.common.base.Strings;
import lombok.Value;
import lombok.experimental.Accessors;
import net.silthus.skills.TestResult;
import net.silthus.skills.entities.ConfiguredSkill;
import net.silthus.skills.entities.PlayerSkill;
import net.silthus.skills.entities.SkilledPlayer;
import net.silthus.skills.events.AddPlayerSkillEvent;
import org.bukkit.Bukkit;

@Value
@Accessors(fluent = true)
public class AddSkillAction {

    SkilledPlayer player;
    ConfiguredSkill skill;

    public Result execute(boolean bypassChecks) {

        if (player.hasActiveSkill(skill)) {
            return new Result(this, player.name() + " hat bereits den Skill: " + skill.alias());
        }

        AddPlayerSkillEvent event = new AddPlayerSkillEvent(this, bypassChecks);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return new Result(this, "Das Hinzufügen des Skills " + skill.alias() + " wurde durch ein Plugin verhindert.");
        }

        TestResult testResult = skill.test(player);
        if (!testResult.success() && !event.isBypassChecks()) {
            return new Result(this, testResult, "Die Vorraussetzungen für den Skill " + skill.alias() + " sind nicht erfüllt.");
        }

        PlayerSkill playerSkill = PlayerSkill.getOrCreate(player, skill);

        playerSkill.unlock();
        playerSkill.activate();

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
            playerSkill = null;
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
            playerSkill = null;
        }

        public boolean success() {
            return Strings.isNullOrEmpty(error);
        }
    }
}
