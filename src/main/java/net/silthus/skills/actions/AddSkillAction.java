package net.silthus.skills.actions;

import com.google.common.base.Strings;
import de.slikey.effectlib.effect.DnaEffect;
import lombok.Value;
import lombok.experimental.Accessors;
import net.silthus.skills.SkillsPlugin;
import net.silthus.skills.TestResult;
import net.silthus.skills.entities.ConfiguredSkill;
import net.silthus.skills.entities.PlayerSkill;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

@Value
@Accessors(fluent = true)
public class AddSkillAction {

    SkilledPlayer player;
    ConfiguredSkill skill;

    public Result execute(boolean bypassChecks) {

        if (player.hasActiveSkill(skill)) {
            return new Result(this, player.name() + " hat bereits den Skill: " + skill.alias());
        }

        TestResult testResult = skill.test(player);
        if (!testResult.success() && !bypassChecks) {
            return new Result(this, testResult, "Die Vorraussetzungen für den Skill " + skill.alias() + " sind nicht erfüllt.");
        }

        PlayerSkill playerSkill = PlayerSkill.getOrCreate(player, skill);

        if (!playerSkill.unlocked()) {
            playerSkill.unlock();
            player.getBukkitPlayer().ifPresent(p -> {
                DnaEffect effect = new DnaEffect(SkillsPlugin.instance().getEffectManager());
                effect.setLocation(p.getLocation());
                effect.setTargetLocation(p.getLocation().add(0, 2, 0));
                effect.duration = 2000;
                effect.start();
                p.playSound(p.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, SoundCategory.MASTER, 10f, 1f);
                p.sendMessage(ChatColor.GREEN + "Du hast den Skill " + playerSkill.skill().name() + " freigeschaltet!");
            });
        }

        if (!playerSkill.active()) {
            playerSkill.activate();
        }

        player.save();
        return new Result(this, testResult);
    }

    @Value
    @Accessors(fluent = true)
    public static class Result {

        AddSkillAction action;
        TestResult testResult;
        String error;

        public Result(AddSkillAction action, TestResult testResult, String error) {
            this.action = action;
            this.testResult = testResult;
            this.error = error;
        }

        public Result(AddSkillAction action, TestResult testResult) {
            this.action = action;
            this.testResult = testResult;
            this.error = null;
        }

        public Result(AddSkillAction action, String error) {
            this.action = action;
            this.testResult = null;
            this.error = error;
        }

        public boolean success() {
            return Strings.isNullOrEmpty(error);
        }
    }
}
