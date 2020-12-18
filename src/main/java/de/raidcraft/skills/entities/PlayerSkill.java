package de.raidcraft.skills.entities;

import de.raidcraft.skills.Skill;
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

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
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

    Optional<Skill> skill() {

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

        skill().ifPresent(Skill::apply);
    }

    public void disable() {

        if (!active()) return;

        skill().ifPresent(Skill::remove);
    }

    public boolean canActivate() {

        boolean bypassSkillLimit = player().getBukkitPlayer()
                .map(p -> p.hasPermission(SkillsPlugin.BYPASS_ACTIVE_SKILL_LIMIT))
                .orElse(false);

        return !active()
                && (bypassSkillLimit || player().freeSkillSlots() >= configuredSkill().skillslots());
    }

    public void activate() {

        if (!canActivate()) return;

        try {
            PlayerActivateSkillEvent event = new PlayerActivateSkillEvent(player(), this);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) return;

            status(SkillStatus.ACTIVE);
            save();

            if (event.isPlayEffect()) {
                player().getBukkitPlayer().ifPresent(Effects::playerActivateSkill);
            }

            enable();

            Bukkit.getPluginManager().callEvent(new PlayerActivatedSkillEvent(player(), this));
        } catch (Exception e) {
            log.severe("An error occured while activating the skill " + alias() + " of " + player().name() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deactivate() {
        if (!active()) return;

        try {
            status(SkillStatus.UNLOCKED);
            save();

            disable();
        } catch (Exception e) {
            log.severe("An error occured while deactivating the skill " + alias() + " of " + player().name() + ": " + e.getMessage());
            e.printStackTrace();
        }
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
            player().getBukkitPlayer().ifPresent(player -> {
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
