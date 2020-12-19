package de.raidcraft.skills;

import de.raidcraft.skills.entities.PlayerSkill;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import net.silthus.configmapper.ConfigurationException;
import net.silthus.configmapper.bukkit.BukkitConfigMap;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Log(topic = "RCSkills")
class DefaultSkillContext implements SkillContext {

    private final UUID playerSkillId;
    private final PlayerSkill playerSkill;
    private final Skill.Registration<?> registration;
    private Skill skill;
    private long interval;
    private boolean enabled;
    private BukkitTask task;

    DefaultSkillContext(PlayerSkill playerSkill, Skill.Registration<?> registration) {
        this.playerSkillId = playerSkill.id();
        this.playerSkill = playerSkill;
        this.registration = registration;
        this.interval = registration().info().taskInterval();
    }

    @Override
    public PlayerSkill playerSkill() {

        playerSkill.refresh();
        return playerSkill;
    }

    DefaultSkillContext init() throws ConfigurationException {

        skill = registration.supplier().apply(this);
        this.interval = playerSkill.configuredSkill().getConfig().getLong("task.interval", interval);
        ConfigurationSection skillConfig = playerSkill.configuredSkill().getSkillConfig();
        skill.load(skillConfig);
        skill = BukkitConfigMap.of(skill)
                .with(skillConfig)
                .applyTo(skill);
        return this;
    }

    public Skill get() {
        try {
            if (skill == null) {
                init();
            }
        } catch (ConfigurationException e) {
            log.severe("Failed to load skill " + registration.skillClass().getCanonicalName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return skill;
    }

    @Override
    public void enable() {

        if (enabled()) return;

        Skill skill = get();
        skill.apply();

        if (skill instanceof Periodic) {
            task = Bukkit.getScheduler().runTaskTimer(
                    SkillsPlugin.instance(),
                    ((Periodic) skill)::tick,
                    interval,
                    interval
            );
        } else if (skill instanceof PeriodicAsync) {
            task = Bukkit.getScheduler().runTaskTimerAsynchronously(
                    SkillsPlugin.instance(),
                    ((PeriodicAsync) skill)::tickAsync,
                    interval,
                    interval
            );
        }

        enabled(true);
    }

    @Override
    public void disable() {

        if (!enabled()) return;

        if (task != null) {
            task.cancel();
            task = null;
        }

        get().remove();

        enabled(false);
    }
}
