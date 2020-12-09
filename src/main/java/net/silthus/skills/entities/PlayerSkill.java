package net.silthus.skills.entities;

import io.ebean.Finder;
import io.ebean.annotation.Index;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import net.silthus.skills.SkillStatus;

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

    public PlayerSkill() {

    }

    PlayerSkill(SkilledPlayer player, ConfiguredSkill skill) {
        this.player = player;
        this.skill = skill;
    }

    public boolean unlocked() {
        return status != null && status.isUnlocked();
    }

    public boolean active() {
        return status != null && status.isActive();
    }

    public void activate() {
        if (!unlocked()) return;

        skill().getSkill().ifPresent(s -> s.apply(player()));
        status(SkillStatus.ACTIVE);
        save();
    }

    public void deactivate() {
        if (!active()) return;

        skill.getSkill().ifPresent(s -> s.remove(player()));
        status(SkillStatus.INACTIVE);
        save();
    }

    public void unlock() {

        status(SkillStatus.UNLOCKED);
        save();
    }

    @Override
    public boolean delete() {

        status(SkillStatus.REMOVED);
        skill().getSkill().ifPresent(skill -> skill.remove(player()));
        save();

        return true;
    }
}
