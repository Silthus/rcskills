package net.silthus.skills.entities;

import io.ebean.Finder;
import io.ebean.annotation.History;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Getter
@Setter
@Accessors(fluent = true)
@Table(name = "rcs_player_levels")
public class PlayerLevel extends BaseEntity {

    public static final Finder<UUID, PlayerLevel> find = new Finder<>(PlayerLevel.class);

    public static PlayerLevel getOrCreate(SkilledPlayer player) {

        return find.query().where()
                .eq("player_id", player.id())
                .findOneOrEmpty()
                .orElse(new PlayerLevel(player));
    }

    @OneToOne(optional = false)
    private SkilledPlayer player;
    private long level = 1;
    private long totalExp = 0;
    private long skillPoints = 0;

    private PlayerLevel(SkilledPlayer player) {

        this.player = player;
    }

    public PlayerLevel addExp(long exp) {
        totalExp += exp;
        return this;
    }

    public PlayerLevel addLevel(int level) {
        this.level += level;
        return this;
    }

    public PlayerLevel addSkillPoints(int skillPoints) {
        this.skillPoints += skillPoints;
        return this;
    }
}
