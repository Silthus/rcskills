package de.raidcraft.skills;

import de.raidcraft.skills.entities.SkilledPlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class SkillsExpansion extends PlaceholderExpansion {

    @Override
    public boolean canRegister() {

        return true;
    }

    @Override
    public @NotNull String getIdentifier() {

        return "rcskills";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Silthus";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier) {

        SkilledPlayer skilledPlayer = SkilledPlayer.getOrCreate(player);
        if (identifier.equals("level")) {
            return String.valueOf(skilledPlayer.level().getLevel());
        }
        if (identifier.equals("total_exp")) {
            return String.valueOf(skilledPlayer.level().getTotalExp());
        }
        if (identifier.equals("skillpoints")) {
            return String.valueOf(skilledPlayer.skillPoints());
        }
        if (identifier.equals("slots")) {
            return String.valueOf(skilledPlayer.slotCount());
        }
        if (identifier.equals("free_slots")) {
            return String.valueOf(skilledPlayer.freeSkillSlots());
        }
        if (identifier.equals("active_slots")) {
            return String.valueOf(skilledPlayer.activeSlotCount());
        }
        if (identifier.equals("free_resets")) {
            return String.valueOf(skilledPlayer.freeResets());
        }
        if (identifier.equals("active_skills")) {
            return String.valueOf(skilledPlayer.activeSkills().size());
        }
        if (identifier.equals("skills")) {
            return String.valueOf(skilledPlayer.skillCount());
        }

        return null;
    }
}
