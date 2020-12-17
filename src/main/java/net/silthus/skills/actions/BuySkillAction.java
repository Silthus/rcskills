package net.silthus.skills.actions;

import com.google.common.base.Strings;
import de.raidcraft.economy.Economy;
import io.ebean.annotation.Transactional;
import lombok.Value;
import lombok.experimental.Accessors;
import net.silthus.skills.TestResult;
import net.silthus.skills.entities.ConfiguredSkill;
import net.silthus.skills.entities.PlayerSkill;
import net.silthus.skills.entities.SkilledPlayer;
import net.silthus.skills.events.BuyPlayerSkillEvent;
import org.bukkit.Bukkit;

import java.util.Map;

@Value
@Accessors(fluent = true)
public class BuySkillAction {

    SkilledPlayer player;
    ConfiguredSkill skill;

    /**
     * Executes the buy skill action, checking if the skill can be bought and subtracting any costs.
     * <p>You can pass bypassChecks true to bypass the cost check and charging of costs.
     *
     * @param bypassChecks true to bypass any requirement and cost checks
     * @return the result of the buy action
     */
    @Transactional
    public Result execute(boolean bypassChecks) {

        if (player.hasActiveSkill(skill)) {
            return new Result(this, player.name() + " hat bereits den Skill: " + skill.alias());
        }

        BuyPlayerSkillEvent event = new BuyPlayerSkillEvent(this, bypassChecks);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return new Result(this, "Das Hinzufügen des Skills " + skill.alias() + " wurde durch ein Plugin verhindert.");
        }

        TestResult testResult = skill.test(player);
        if (!testResult.success() && !event.isBypassChecks()) {
            return new Result(this, testResult, "Die Vorraussetzungen für den Skill " + skill.alias() + " sind nicht erfüllt.");
        }

        if (!bypassChecks) {
            player.removeSkillPoints(skill.skillpoints());
            Economy.get().withdrawPlayer(player.getOfflinePlayer(), skill.money(), "Skill gekauft: " + skill.name(), Map.of(
                    "skill", skill.alias(),
                    "skill_id", skill.id()
            ));
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

        BuySkillAction action;
        PlayerSkill playerSkill;
        TestResult testResult;
        String error;

        public Result(BuySkillAction action, TestResult testResult, String error) {
            this.action = action;
            this.testResult = testResult;
            this.error = error;
            playerSkill = null;
        }

        public Result(BuySkillAction action, PlayerSkill playerSkill, TestResult testResult) {
            this.action = action;
            this.playerSkill = playerSkill;
            this.testResult = testResult;
            this.error = null;
        }

        public Result(BuySkillAction action, String error) {
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
