package de.raidcraft.skills;

import de.raidcraft.skills.entities.PlayerSkill;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import net.silthus.configmapper.ConfigurationException;
import net.silthus.configmapper.bukkit.BukkitConfigMap;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.Optional;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Log(topic = "RCSkills")
@ToString(of = { "playerSkillId", "registration", "enabled" })
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

    public void reload() {

        try {
            disable();
            init();
            enable();
        } catch (ConfigurationException e) {
            log.severe("failed to reload skill " + configuredSkill().alias() + " for " + skilledPlayer().name() + ": " + e.getMessage());
            e.printStackTrace();
        }
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

    public PlayerSkill playerSkill() {

        playerSkill.refresh();
        return playerSkill;
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

        if (skill instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) skill, SkillsPlugin.instance());
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

        Skill skill = get();
        if (skill instanceof Listener) {
            HandlerList.unregisterAll((Listener) skill);
        }
        skill.remove();

        enabled(false);
    }

    @Override
    public ExecutionResult execute() {

        ExecutionContext context = ExecutionContext.of(this);

        Skill skill = get();
        if (!(skill instanceof Executable)) {
            return context.failure("Skill is not executable.");
        }

        Optional<Player> player = player();
        if (player.isEmpty()) {
            return context.failure("Der Besitzer des Skills ist nicht online.");
        }

        try {
            return ((Executable) skill).execute(context);
        } catch (Exception e) {
            log.severe("an error occurred while executing skill \"" + configuredSkill().name() + " (" + configuredSkill().alias() + "): " + e.getMessage());
            e.printStackTrace();
            return context.failure(e.getMessage());
        }
    }
}
