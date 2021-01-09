package de.raidcraft.skills.listener;

import de.raidcraft.skills.*;
import de.raidcraft.skills.entities.PlayerSkill;
import de.raidcraft.skills.entities.SkilledPlayer;
import de.raidcraft.skills.events.EnableSkillEvent;
import de.raidcraft.skills.events.SkillStatusChangedEvent;
import de.raidcraft.skills.util.Effects;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collection;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class PlayerListener implements Listener {

    private final SkillsPlugin plugin;
    private final SkillManager skillManager;

    public PlayerListener(SkillsPlugin plugin, SkillManager skillManager) {
        this.plugin = plugin;
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

    @EventHandler(priority = EventPriority.HIGH)
    public void onEnableSkill(EnableSkillEvent event) {

        if (isDisabled(event.getSkill())) {
            event.setCancelled(true);
            Messages.send(event.getPlayer(), text().append(text("Du kannst den Skill ", RED))
                    .append(Messages.skill(event.getSkill(), false))
                    .append(text(" in dieser Welt nicht nutzen.", RED))
                    .build()
            );
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChangeWorld(PlayerChangedWorldEvent event) {

        SkilledPlayer.getOrCreate(event.getPlayer())
                .activeSkills()
                .forEach(skill -> {
                    if (isDisabled(skill)) {
                        skill.disable();
                        Messages.send(event.getPlayer(), text().append(text("Du kannst den Skill ", RED))
                                .append(Messages.skill(skill, false))
                                .append(text(" in dieser Welt nicht nutzen.", RED))
                                .build()
                        );
                    } else {
                        skill.enable();
                    }
                });
    }

    @EventHandler(ignoreCancelled = true)
    public void onSkillActivation(SkillStatusChangedEvent event) {

        event.getPlayer().bukkitPlayer().ifPresent(player -> {
            if (event.getStatus() == SkillStatus.ACTIVE) {
                Effects.activateSkill(player);
            }
        });
    }

    private boolean isDisabled(PlayerSkill skill) {

        return skill.player().bukkitPlayer().map(player -> {
            String world = player.getWorld().getName().toLowerCase();

            Collection<String> disabledWorlds = skill.configuredSkill().disabledWorlds();
            if (disabledWorlds.size() > 0) {
                return disabledWorlds.contains(world);
            }

            Collection<String> worlds = skill.configuredSkill().worlds();
            if (worlds.size() > 0) {
                return !worlds.contains(world);
            }

            for (SkillPluginConfig.DisableConfig config : plugin.getPluginConfig().getDisabled()) {
                if (world.equalsIgnoreCase(config.getWorld())) {
                    return !config.getExclude().contains(skill.alias());
                }
            }

            return false;
        }).orElse(false);
    }
}
