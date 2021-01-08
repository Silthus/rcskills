package de.raidcraft.skills.entities;

import de.raidcraft.skills.ExecutionResult;
import de.raidcraft.skills.SkillContext;
import de.raidcraft.skills.SkillStatus;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.events.*;
import io.ebean.Finder;
import io.ebean.annotation.DbDefault;
import io.ebean.annotation.Index;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import net.silthus.ebean.BaseEntity;
import org.bukkit.Bukkit;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

    public static Optional<PlayerSkill> find(SkilledPlayer player, ConfiguredSkill skill) {

        return find.query()
                .where().eq("player_id", player.id())
                .and().eq("configured_skill_id", skill.id())
                .findOneOrEmpty();
    }

    public static PlayerSkill getOrCreate(SkilledPlayer player, ConfiguredSkill skill) {

        return find(player, skill)
                .orElseGet(() -> {
                    PlayerSkill playerSkill = new PlayerSkill(player, skill);
                    playerSkill.insert();

                    if (skill.isChild()) {
                        playerSkill.parent(PlayerSkill.getOrCreate(player, skill.parent()));
                    }

                    if (skill.isParent()) {
                        for (ConfiguredSkill child : skill.children()) {
                            playerSkill.children().add(PlayerSkill.getOrCreate(player, child));
                        }
                    }

                    playerSkill.save();

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
    @DbDefault("false")
    private boolean replaced = false;
    @DbDefault("false")
    @Setter(AccessLevel.PACKAGE)
    private boolean disabled = false;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private DataStore data = new DataStore();

    @ManyToOne
    private PlayerSkill parent;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayerSkill> children = new ArrayList<>();

    PlayerSkill(SkilledPlayer player, ConfiguredSkill configuredSkill) {
        this.player = player;
        this.configuredSkill = configuredSkill;
    }

    public String alias() {
        return configuredSkill().alias();
    }

    public String name() {
        return configuredSkill().name();
    }

    public String description() {
        return configuredSkill().description();
    }

    public boolean isChild() {

        return parent() != null;
    }

    public boolean isParent() {

        return !children().isEmpty();
    }

    public boolean enabled() {

        return !disabled() && !replaced();
    }

    /**
     * Enables this player skill allowing it to be applied to the player.
     * <p>If it is active it is immediately to the player.
     *
     * @return this player skill
     */
    public PlayerSkill enable() {

        disabled(false);
        save();
        attach();

        return this;
    }

    /**
     * Will disable this player skill.
     * <p>Immediately removes the skill from the player if it is active.
     *
     * @return this player skill
     */
    public PlayerSkill disable() {

        disabled(true);
        save();
        detach();

        return this;
    }

    /**
     * Marks the skill as replaced by another skill and applies or removes it from the player.
     * <p>A replaced skill is effectively disabled and cannot be applied to players.
     *
     * @param replaced true to replace this skill disabling and removing it from the player
     * @return this skill
     */
    PlayerSkill replaced(boolean replaced) {

        if (replaced() == replaced) return this;

        this.replaced = replaced;
        save();

        if (replaced) {
            detach();
        } else {
            attach();
        }

        return this;
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

    public boolean executable() {

        return context().map(SkillContext::executable).orElse(false);
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

    /**
     * Attaches this skill to the player if the skill is active and enabled.
     * <p>Will also check if the skill needs to be disabled or deactivated and do so.
     * <p>All child skills will be applied as well.
     */
    public void attach() {

        if (!active()) return;
        if (checkDeactivate()) return;
        if (checkDisable()) return;

        if (enabled()) {
            context().ifPresent(SkillContext::attach);
        }
        children().forEach(PlayerSkill::attach);
    }

    /**
     * Detaches the skill from the player if it is active.
     * <p>Will also remove all child skills that are active and applied.
     */
    public void detach() {

        if (!active()) return;

        children().forEach(PlayerSkill::detach);
        context().ifPresent(SkillContext::detach);
    }

    public void reload() {

        if (!activate()) return;
        if (checkDeactivate()) return;
        if (checkDisable()) return;

        if (enabled()) {
            context().ifPresent(SkillContext::reload);
        }
        children().forEach(PlayerSkill::reload);
    }

    public void execute(Consumer<ExecutionResult> callback) {

        if (!active()) {
            callback.accept(ExecutionResult.failure(null, "Der Skill ist nicht aktiv."));
            return;
        }

        if (enabled()) {
            context().ifPresentOrElse(context -> context.execute(callback),
                    () -> callback.accept(ExecutionResult.failure(null, "Der Skill konnte nicht geladen werden.")));
        }

        children().stream()
                .filter(PlayerSkill::active)
                .forEach(skill -> skill.execute(callback));
    }

    public boolean canActivate() {

        if (!unlocked()) return false;
        if (isChild() && !parent().active()) return false;
        if (configuredSkill().disabled()) return false;
        if (active()) return false;
        if (configuredSkill().noSkillSlot()) return true;
        if (player().hasFreeSkillSlot()) return true;

        return player().bukkitPlayer()
                .map(p -> p.hasPermission(SkillsPlugin.BYPASS_ACTIVE_SKILL_LIMIT))
                .orElse(false);
    }

    public boolean activate() {

        if (checkDeactivate()) return false;

        if (!canActivate()) {
            return false;
        }

        try {
            PlayerActivateSkillEvent event = new PlayerActivateSkillEvent(player(), this);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) return false;

            if (configuredSkill.autoActivate()) {
                if (!configuredSkill.noSkillSlot())
                    player().freeSkillSlot().assign(this).save();
            }

            refresh();
            status(SkillStatus.ACTIVE);
            save();

            configuredSkill().replacedSkills().stream()
                    .map(skill -> player().getSkill(skill))
                    .forEach(skill -> skill.replaced(true));

            attach();

            Bukkit.getPluginManager().callEvent(new PlayerActivatedSkillEvent(player(), this));

            children().forEach(PlayerSkill::activate);

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
            detach();

            SkillSlot.of(this).ifPresent(skillSlot -> skillSlot.unassign().save());
            status(SkillStatus.UNLOCKED);
            save();

            player().bindings().unbind(this);
            Bukkit.getPluginManager().callEvent(new PlayerDeactivatedSkillEvent(player(), this));

            configuredSkill().replacedSkills().stream()
                    .map(skill -> player().getSkill(skill))
                    .forEach(skill -> skill.replaced(false));

            children().forEach(PlayerSkill::deactivate);
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

        refresh();
        status(SkillStatus.UNLOCKED);
        save();

        children().stream()
                .filter(skill -> skill.configuredSkill().canAutoUnlock(player()))
                .forEach(PlayerSkill::unlock);

        if (configuredSkill().autoActivate() || configuredSkill().noSkillSlot()) {
            if (!isChild() || parent().active())
                activate();
        }

        Bukkit.getPluginManager().callEvent(new PlayerUnlockedSkillEvent(player(), this));

        return true;
    }

    @Override
    public boolean delete() {

        context().ifPresent(SkillContext::detach);

        return super.delete();
    }

    /**
     * Checks if the skill needs to be deactivated and deactivates it.
     * <p>A skill should be considered disabled if the underlying configured skill is disabled.
     *
     * @return true if the skill should be disabled and was disabled
     */
    private boolean checkDeactivate() {

        if (configuredSkill.disabled() && active()) {
            deactivate();
            return true;
        }
        return false;
    }

    private boolean checkDisable() {

        if (disabled()) {
            detach();
            return true;
        }
        return false;
    }
}
