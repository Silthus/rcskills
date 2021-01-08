package de.raidcraft.skills.listener;

import de.raidcraft.skills.Messages;
import de.raidcraft.skills.SkillManager;
import de.raidcraft.skills.SkillStatus;
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

import java.util.List;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

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

    @EventHandler(priority = EventPriority.HIGH)
    public void onEnableSkill(EnableSkillEvent event) {

        if (isDisabled(event.getSkill())) {
            event.setCancelled(true);
            Messages.send(event.getPlayer(), text().append(text("Du kannst den Skill ", RED))
                    .append(Messages.skill(event.getSkill(), false))
                    .append(text(" in dieser Welt nicht ausführen.", RED))
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
                                .append(text(" in dieser Welt nicht ausführen.", RED))
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

            List<String> worlds = skill.configuredSkill().worlds();
            if (worlds.size() > 0) {
                return !worlds.contains(world);
            }

            List<String> disabledWorlds = skill.configuredSkill().disabledWorlds();
            if (disabledWorlds.size() > 0) {
                return disabledWorlds.contains(world);
            }

            return false;
        }).orElse(false);
    }
}
