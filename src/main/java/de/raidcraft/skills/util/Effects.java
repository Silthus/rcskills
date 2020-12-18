package de.raidcraft.skills.util;

import de.raidcraft.skills.SkillsPlugin;
import de.slikey.effectlib.effect.DnaEffect;
import lombok.NonNull;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public final class Effects {

    private Effects() {
    }

    public static void playerUnlockSkill(@NonNull Player player) {

        DnaEffect effect = new DnaEffect(SkillsPlugin.instance().getEffectManager());
        effect.setLocation(player.getLocation());
        effect.setTargetLocation(player.getLocation().add(0, 2, 0));
        effect.duration = 2000;
        effect.start();
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, SoundCategory.MASTER, 10f, 1f);
    }

    public static void playerActivateSkill(Player player) {

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.MASTER, 10f, 1f);
    }
}
