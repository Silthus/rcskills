package de.raidcraft.skills.listener;

import de.raidcraft.skills.SkillManager;
import de.raidcraft.skills.entities.SkilledPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class PlayerListener implements Listener {

    private final SkillManager skillManager;

    public PlayerListener(SkillManager skillManager) {
        this.skillManager = skillManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {

        skillManager.load(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {

        skillManager.unload(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onChangeWorld(PlayerChangedWorldEvent event) {

        SkilledPlayer.getOrCreate(event.getPlayer())
                .activeSkills()
                .forEach(skill -> {
                    List<String> worlds = skill.configuredSkill().worlds();
                    if (worlds.size() > 0 && !worlds.contains(event.getPlayer().getWorld().getName().toLowerCase())) {
                        skill.disable();
                    } else {
                        skill.enable();
                    }
                });
    }
}
