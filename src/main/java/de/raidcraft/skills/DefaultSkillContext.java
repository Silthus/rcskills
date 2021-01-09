package de.raidcraft.skills;

import de.raidcraft.skills.entities.ConfiguredSkill;
import de.raidcraft.skills.entities.PlayerSkill;
import de.raidcraft.skills.entities.SkilledPlayer;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Data
@Accessors(fluent = true)
@Log(topic = "RCSkills")
@ToString(of = { "playerSkillId", "registration", "attached" })
class DefaultSkillContext implements SkillContext {

    private final UUID playerSkillId;
    private final UUID playerId;
    private final UUID configuredSkillId;
    private final Skill.Registration<?> registration;
    private Skill skill;
    private long interval;
    private boolean attached;
    private BukkitTask task;

    DefaultSkillContext(PlayerSkill playerSkill, Skill.Registration<?> registration) {
        this.playerSkillId = playerSkill.id();
        this.playerId = playerSkill.player().id();
        this.configuredSkillId = playerSkill.configuredSkill().id();
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
        PlayerSkill playerSkill = playerSkill();

        if (playerSkill == null) {
            throw new ConfigurationException("the player skill has been deleted.");
        }

        this.interval = playerSkill.configuredSkill().taskConfig().interval(interval);
        ConfigurationSection skillConfig = playerSkill.configuredSkill().getSkillConfig();
        skill = BukkitConfigMap.of(skill)
                .with(skillConfig)
                .applyTo(skill);
        skill.load(skillConfig);
        return this;
    }

    @Override
    public SkilledPlayer skilledPlayer() {

        return SkilledPlayer.find.byId(playerId);
    }

    @Override
    public ConfiguredSkill configuredSkill() {

        return ConfiguredSkill.find.byId(configuredSkillId);
    }

    public PlayerSkill playerSkill() {

        return PlayerSkill.find.byId(playerSkillId);
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

        if (attached()) return;

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

        attached(true);
    }

    @Override
    public void disable() {

        if (!attached()) return;

        if (task != null) {
            task.cancel();
            task = null;
        }

        Skill skill = get();
        if (skill instanceof Listener) {
            HandlerList.unregisterAll((Listener) skill);
        }
        skill.remove();

        attached(false);
    }

    @Override
    public void execute(Consumer<ExecutionResult> callback) {

        ExecutionContext context = ExecutionContext.of(this, callback);

        if (!executable()) {
            return;
        }

        Optional<Player> player = player();
        if (player.isEmpty()) {
            callback.accept(context.failure("Der Besitzer des Skills ist nicht online."));
            return;
        }

        if (isOnCooldown()) {
            callback.accept(context.cooldown());
            return;
        }

        if (context.config().delay() > 0) {
            Bukkit.getScheduler().runTaskLater(SkillsPlugin.instance(),
                    context,
                    context.config().delay()
            );
            callback.accept(context.delayed());
        } else {
            context.run();
        }
    }

    @Override
    public long getRemainingCooldown() {

        if (configuredSkill().executionConfig().cooldown() > 0) {
            return getCooldown().toEpochMilli() - Instant.now().toEpochMilli();
        }

        return -1;
    }

    private Instant getCooldown() {

        return playerSkill().lastUsed()
                .plus(configuredSkill().executionConfig().cooldown(), ChronoUnit.MILLIS);
    }
}
