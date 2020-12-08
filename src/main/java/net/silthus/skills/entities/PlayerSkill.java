package net.silthus.skills.entities;

import io.ebean.Finder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import net.silthus.skills.SkillsPlugin;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "rcs_player_skills")
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

    @ManyToOne
    private SkilledPlayer player;
    @ManyToOne
    private ConfiguredSkill skill;
    private Instant unlocked = null;
    private boolean active = false;

    public PlayerSkill() {

    }

    PlayerSkill(SkilledPlayer player, ConfiguredSkill skill) {
        this.player = player;
        this.skill = skill;
    }

    public boolean unlocked() {
        return this.unlocked != null;
    }

    public void unlock() {
        if (unlocked != null) return;
        unlocked = Instant.now();
        save();
    }
}
