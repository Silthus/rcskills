package de.raidcraft.skills.entities;

import de.raidcraft.skills.SkillStatus;
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
import net.silthus.ebean.BaseEntity;
import org.bukkit.Bukkit;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.UUID;

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
                    playerSkill.save();
                    return playerSkill;
                });
    }

    public static final Finder<UUID, PlayerSkill> find = new Finder<>(PlayerSkill.class);

    @ManyToOne
    private SkilledPlayer player;
    @ManyToOne
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

    public void disable() {

        if (!active()) return;

        configuredSkill().remove(player());
        configuredSkill().enabled(false).save();
    }

    public void activate() {

        if (active()) return;

        try {
            PlayerActivateSkillEvent event = new PlayerActivateSkillEvent(player(), this);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) return;

            configuredSkill().apply(player());
            status(SkillStatus.ACTIVE);
            save();

            if (event.isPlayEffect()) {
                player().getBukkitPlayer().ifPresent(Effects::playerActivateSkill);
            }

            Bukkit.getPluginManager().callEvent(new PlayerActivatedSkillEvent(player(), this));
        } catch (Exception e) {
            log.severe("An error occured while activating the skill " + alias() + " of " + player().name() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deactivate() {
        if (!active()) return;

        try {
            status(SkillStatus.INACTIVE);
            save();

            configuredSkill.remove(player());
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
            player().getBukkitPlayer().ifPresent(Effects::playerUnlockSkill);
        }

        Bukkit.getPluginManager().callEvent(new PlayerUnlockedSkillEvent(player(), this));

        return true;
    }

    @Override
    public boolean delete() {

        status(SkillStatus.REMOVED);
        configuredSkill().skill().ifPresent(skill -> skill.remove(player()));
        save();

        return true;
    }
}
