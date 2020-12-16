package net.silthus.skills.entities;

import io.ebean.Finder;
import io.ebean.annotation.Index;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import net.silthus.skills.SkillStatus;
import net.silthus.skills.events.PlayerActivateSkillEvent;
import net.silthus.skills.events.PlayerActivatedSkillEvent;
import net.silthus.skills.events.PlayerUnlockSkillEvent;
import net.silthus.skills.events.PlayerUnlockedSkillEvent;
import net.silthus.skills.util.Effects;
import org.bukkit.Bukkit;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "rcs_player_skills")
@Index(columnNames = {"player_id", "skill_id"})
@Accessors(fluent = true)
public class PlayerSkill extends BaseEntity {

    public static PlayerSkill getOrCreate(SkilledPlayer player, ConfiguredSkill skill) {

        return find.query()
                .where().eq("player_id", player.id())
                .and().eq("skill_id", skill.id())
                .findOneOrEmpty()
                .orElseGet(() -> {
                    PlayerSkill playerSkill = new PlayerSkill(player, skill);
                    playerSkill.save();
                    return playerSkill;
                });
    }

    public static final Finder<UUID, PlayerSkill> find = new Finder<>(PlayerSkill.class);

    @ManyToOne(fetch = FetchType.EAGER)
    private SkilledPlayer player;
    @ManyToOne(fetch = FetchType.EAGER)
    private ConfiguredSkill skill;
    private SkillStatus status = SkillStatus.REMOVED;

    PlayerSkill(SkilledPlayer player, ConfiguredSkill skill) {
        this.player = player;
        this.skill = skill;
    }

    public String alias() {
        return skill.alias();
    }

    public String name() {
        return skill.name();
    }

    public String description() {
        return skill.description();
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

    public boolean activate() {
        if (active()) return false;

        PlayerActivateSkillEvent event = new PlayerActivateSkillEvent(player(), this);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return false;

        skill().getSkill().ifPresent(s -> s.apply(player()));
        status(SkillStatus.ACTIVE);
        save();

        if (event.isPlayEffect()) {
            player().getBukkitPlayer().ifPresent(Effects::playerActivateSkill);
        }

        Bukkit.getPluginManager().callEvent(new PlayerActivatedSkillEvent(player(), this));

        return true;
    }

    public void deactivate() {
        if (!active()) return;

        status(SkillStatus.INACTIVE);
        save();

        skill.getSkill().ifPresent(s -> s.remove(player()));
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
        skill().getSkill().ifPresent(skill -> skill.remove(player()));
        save();

        return true;
    }
}
