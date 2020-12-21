package de.raidcraft.skills.entities;

import de.raidcraft.skills.SkillContext;
import de.raidcraft.skills.SkillStatus;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.events.PlayerActivateSkillEvent;
import de.raidcraft.skills.events.PlayerActivatedSkillEvent;
import de.raidcraft.skills.events.PlayerUnlockSkillEvent;
import de.raidcraft.skills.events.PlayerUnlockedSkillEvent;
import de.raidcraft.skills.util.Effects;
import io.ebean.Finder;
import io.ebean.annotation.Index;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.silthus.ebean.BaseEntity;
import org.bukkit.Bukkit;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Optional;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

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
    @OneToOne(cascade = CascadeType.ALL)
    private DataStore data = new DataStore();

    PlayerSkill(SkilledPlayer player, ConfiguredSkill configuredSkill) {
        this.player = player;
        this.configuredSkill = configuredSkill;
    }

    public DataStore data() {
        if (data == null) {
            data = new DataStore();
            save();
        }
        return data;
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

    Optional<SkillContext> context() {

        return Optional.ofNullable(SkillsPlugin.instance().getSkillManager().loadSkill(this));
    }

    /**
     * Checks if this skill is unlocked or active.
     *
     * @return true if the skill is unlocked or active
     */
    public boolean unlocked() {
        return status != null && status.isUnlocked();
    }

    /**
     * @return true if the skill is active
     */
    public boolean active() {
        return status != null && status.isActive();
    }

    public void enable() {

        if (!active()) return;

        context().ifPresent(SkillContext::enable);
    }

    public void disable() {

        if (!active()) return;

        context().ifPresent(SkillContext::disable);
    }

    public boolean canActivate() {

        if (!configuredSkill.enabled()) return false;

        boolean bypassSkillLimit = player().bukkitPlayer()
                .map(p -> p.hasPermission(SkillsPlugin.BYPASS_ACTIVE_SKILL_LIMIT))
                .orElse(false);

        return !active()
                && (bypassSkillLimit || configuredSkill().noSkillSlot() || player().hasFreeSkillSlot());
    }

    public PlayerSkill activate() {

        if (!canActivate()) {
            if (active()) deactivate();
            return this;
        }

        try {
            PlayerActivateSkillEvent event = new PlayerActivateSkillEvent(player(), this);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) return this;

            if (!configuredSkill.noSkillSlot()) {
                player.freeSkillSlot().assign(this).save();
            }

            status(SkillStatus.ACTIVE);
            save();

            enable();

            if (event.isPlayEffect()) {
                player().bukkitPlayer().ifPresent(Effects::playerActivateSkill);
            }

            Bukkit.getPluginManager().callEvent(new PlayerActivatedSkillEvent(player(), this));
        } catch (Exception e) {
            log.severe("An error occured while activating the skill " + alias() + " of " + player().name() + ": " + e.getMessage());
            e.printStackTrace();
            status(SkillStatus.UNLOCKED);
            save();
        }
        return this;
    }

    public PlayerSkill deactivate() {

        if (!active()) return this;

        try {
            SkillSlot.of(this).ifPresent(skillSlot -> skillSlot.unassign().save());
            status(SkillStatus.UNLOCKED);
            save();

            disable();
        } catch (Exception e) {
            log.severe("An error occured while deactivating the skill " + alias() + " of " + player().name() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return this;
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

        if (event.isPlayEffect()) {
            player().bukkitPlayer().ifPresent(player -> {
                Effects.playerUnlockSkill(player);

                TextComponent heading = text("Skill freigeschaltet", NamedTextColor.GOLD);
                TextComponent subheading = text("Skill '", NamedTextColor.GREEN).append(text(name(), NamedTextColor.AQUA))
                        .append(text("' freigeschaltet!", NamedTextColor.GREEN));
                Title title = Title.title(heading, subheading);
                BukkitAudiences.create(SkillsPlugin.instance())
                        .player(player)
                        .showTitle(title);
            });
        }

        Bukkit.getPluginManager().callEvent(new PlayerUnlockedSkillEvent(player(), this));

        return true;
    }

    @Override
    public boolean delete() {

        status(SkillStatus.NOT_PRESENT);
        save();

        disable();

        return true;
    }
}
