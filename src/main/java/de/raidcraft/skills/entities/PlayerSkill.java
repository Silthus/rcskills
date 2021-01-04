package de.raidcraft.skills.entities;

import de.raidcraft.skills.ExecutionResult;
import de.raidcraft.skills.SkillContext;
import de.raidcraft.skills.SkillStatus;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.events.*;
import io.ebean.Finder;
import io.ebean.annotation.Index;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import net.silthus.ebean.BaseEntity;
import org.bukkit.Bukkit;

import javax.persistence.*;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Entity
@Getter
@Setter
@Table(name = "rcs_player_skills")
@Index(columnNames = {"player_id", "configured_skill_id"})
@Accessors(fluent = true)
@Log(topic = "RCSkills")
public class PlayerSkill extends BaseEntity {

    public static PlayerSkill getOrCreate(SkilledPlayer player, ConfiguredSkill skill) {

        return find.query()
                .where().eq("player_id", player.id())
                .and().eq("configured_skill_id", skill.id())
                .findOneOrEmpty()
                .orElseGet(() -> {
                    PlayerSkill playerSkill = new PlayerSkill(player, skill);
                    playerSkill.insert();
                    return playerSkill;
                });
    }

    public static final Finder<UUID, PlayerSkill> find = new Finder<>(PlayerSkill.class);

    @ManyToOne(optional = false)
    private SkilledPlayer player;
    @ManyToOne(optional = false)
    private ConfiguredSkill configuredSkill;
    private SkillStatus status = SkillStatus.NOT_PRESENT;
    private Instant lastUsed = Instant.EPOCH;
    @OneToOne(cascade = CascadeType.ALL)
    private DataStore data = new DataStore();

    PlayerSkill(SkilledPlayer player, ConfiguredSkill configuredSkill) {
        this.player = player;
        this.configuredSkill = configuredSkill;
    }

    public String alias() {
        return configuredSkill.alias();
    }

    public String name() {
        return configuredSkill.name();
    }

    public String description() {
        return configuredSkill.description();
    }

    /**
     * Gets the time the skill was last used.
     * <p>Will never return null and an instant with the start of the epoch instead.
     *
     * @return the last time the skill was used
     */
    public Instant lastUsed() {

        if (lastUsed == null) return Instant.EPOCH;
        return lastUsed;
    }

    Optional<SkillContext> context() {

        return Optional.ofNullable(SkillsPlugin.instance().getSkillManager().loadSkill(this));
    }

    /**
     * Checks if this skill is unlocked or active.
     *
     * @return true if the skill is unlocked or active
     */
    public boolean unlocked() {
        return status() != null && status().isUnlocked();
    }

    /**
     * @return true if the skill is active
     */
    public boolean active() {
        return status() != null && status().isActive();
    }

    public void enable() {

        if (!active()) return;

        if (configuredSkill().disabled()) {
            deactivate();
            return;
        }

        context().ifPresent(SkillContext::enable);
    }

    public void disable() {

        if (!active()) return;

        context().ifPresent(SkillContext::disable);
    }

    public void execute(Consumer<ExecutionResult> callback) {

        if (!active()) {
            callback.accept(ExecutionResult.failure(null, "Der Skill ist nicht aktiv."));
            return;
        }

        context().ifPresentOrElse(context -> context.execute(callback),
                () -> callback.accept(ExecutionResult.failure(null, "Der Skill konnte nicht geladen werden.")));
    }

    public boolean canActivate() {

        if (configuredSkill().disabled()) return false;
        if (active()) return false;
        if (configuredSkill().noSkillSlot()) return true;
        if (player().hasFreeSkillSlot()) return true;

        return player().bukkitPlayer()
                .map(p -> p.hasPermission(SkillsPlugin.BYPASS_ACTIVE_SKILL_LIMIT))
                .orElse(false);
    }

    public boolean activate() {

        if (checkDisable()) return false;

        if (!canActivate()) {
            return false;
        }

        try {
            PlayerActivateSkillEvent event = new PlayerActivateSkillEvent(player(), this);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) return false;

            if (!configuredSkill().noSkillSlot()) {
                player().freeSkillSlot().assign(this).save();
            }

            status(SkillStatus.ACTIVE);
            save();

            context().ifPresent(SkillContext::enable);

            Bukkit.getPluginManager().callEvent(new PlayerActivatedSkillEvent(player(), this));
            return true;
        } catch (Exception e) {
            log.severe("An error occured while activating the skill " + alias() + " of " + player().name() + ": " + e.getMessage());
            e.printStackTrace();
            status(SkillStatus.UNLOCKED);
            save();
        }

        return false;
    }

    public boolean deactivate() {

        if (!active()) return false;

        try {
            SkillSlot.of(this).ifPresent(skillSlot -> skillSlot.unassign().save());
            status(SkillStatus.UNLOCKED);
            save();

            context().ifPresent(SkillContext::disable);
            player().bindings().unbind(this);
            Bukkit.getPluginManager().callEvent(new PlayerDeactivatedSkillEvent(player(), this));
            return true;
        } catch (Exception e) {
            log.severe("An error occured while deactivating the skill " + alias() + " of " + player().name() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean unlock() {

        if (unlocked()) return false;

        PlayerUnlockSkillEvent event = new PlayerUnlockSkillEvent(player(), this);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return false;

        status(SkillStatus.UNLOCKED);
        save();

        // TODO: create own skill history table
//        LevelHistory.create(player())
//                .data("action", "unlocked_skill")
//                .data("skill", name())
//                .data("alias", alias())
//                .save();

        Bukkit.getPluginManager().callEvent(new PlayerUnlockedSkillEvent(player(), this));

        return true;
    }

    @Override
    public boolean delete() {

        disable();

        return super.delete();
    }

    /**
     * Checks if the skill needs to be disabled and disables it.
     * <p>A skill should be considered disabled if the underlying configured skill is disabled.
     *
     * @return true if the skill should be disabled and was disabled
     */
    private boolean checkDisable() {

        if (configuredSkill.disabled() && active()) {
            deactivate();
            return true;
        }
        return false;
    }
}
