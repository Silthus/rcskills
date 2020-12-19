package de.raidcraft.skills.listener;

import de.raidcraft.skills.SkillManager;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.events.PlayerActivateSkillEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
    public void onPlayerActivateSkill(PlayerActivateSkillEvent event) {

        event.getPlayer().bukkitPlayer().ifPresent(player -> {
            if (player.hasPermission(SkillsPlugin.BYPASS_ACTIVE_SKILL_LIMIT)) {
                return;
            }
            if (event.getPlayer().freeSkillSlots() < 1) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Du hast bereits alle Skill Slots belegt und kannst den Skill '"
                        + event.getSkill().name() + "' nicht aktivieren.");
            }
        });

    }
}
