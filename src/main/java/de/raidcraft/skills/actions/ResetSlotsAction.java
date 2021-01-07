package de.raidcraft.skills.actions;

import com.google.common.base.Strings;
import de.raidcraft.economy.wrapper.Economy;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.entities.SkillSlot;
import de.raidcraft.skills.entities.SkilledPlayer;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Map;

@Value
@Accessors(fluent = true)
public class ResetSlotsAction {

    SkilledPlayer player;
    SkillsPlugin plugin;

    public ResetSlotsAction(SkilledPlayer player) {

        this.player = player;
        this.plugin = SkillsPlugin.instance();
    }

    public double cost() {

        return plugin.getSlotManager().calculateSlotResetCost(player);
    }

    public Result execute(boolean bypassChecks) {

        if (player.skillSlots().stream().filter(skillSlot -> skillSlot.status() == SkillSlot.Status.IN_USE).count() < 1) {
            return new Result(this, "Du hast keine Skill Slots die in Benutzung sind.");
        }

        if (!bypassChecks) {
            double cost = cost();
            if (!Economy.get().has(player.offlinePlayer(), cost)) {
                return new Result(this, "Du hast nicht genügend Geld um deine Skill Slots zurückzusetzen. Du benötigst " + Economy.get().format(cost) + ".");
            }

            Economy.get().withdrawPlayer(player.offlinePlayer(), cost, "Skill Slot Reset", Map.of(
                    "player_id", player.id(),
                    "slot_count", player.slotCount(),
                    "skill_count", player.skillCount(),
                    "skill_points", player.skillPoints(),
                    "free_slots", player.freeSkillSlots(),
                    "reset_count", player.resetCount()
            ));
        }

        int slotCount = player.activeSlotCount();

        player.resetSkillSlots();

        if (!bypassChecks && slotCount > plugin.getSlotManager().getFreeResets()) {
            player.resetCount(player.resetCount() + 1);
        }

        plugin.getBindingListener().getUpdateBindings().accept(player.id());

        return new Result(this);
    }


    @Value
    @Accessors(fluent = true)
    public static class Result {

        ResetSlotsAction action;
        String error;

        public Result(ResetSlotsAction action) {

            this.action = action;
            this.error = null;
        }

        public Result(ResetSlotsAction action, String error) {

            this.action = action;
            this.error = error;
        }

        public boolean success() {
            return Strings.isNullOrEmpty(error);
        }

        public boolean failure() {

            return !success();
        }
    }
}
