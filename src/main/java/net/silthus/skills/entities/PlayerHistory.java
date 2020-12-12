package net.silthus.skills.entities;

import io.ebean.annotation.DbJson;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import net.silthus.skills.SkillsPlugin;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@Setter
@Accessors(fluent = true)
@Table(name = "rcs_player_history")
public class PlayerHistory extends BaseEntity {

    public static final String REASON = "reason";

    public static PlayerHistory of(SkilledPlayer level) {

        return new PlayerHistory(level);
    }

    @ManyToOne
    private SkilledPlayer player;
    private int oldLevel;
    private int newLevel;
    private long oldExp;
    private long newExp;
    private int oldSkillPoints;
    private int newSkillPoints;
    @DbJson
    private Map<String, Object> data = new HashMap<>();

    private PlayerHistory(SkilledPlayer player) {
        this.player = player;
        PlayerLevel level = player.level();
        this.oldLevel = level.level();
        this.newLevel = level.level();
        this.oldExp = level.totalExp();
        this.newExp = level.totalExp();
        this.oldSkillPoints = level.skillPoints();
        this.newSkillPoints = level.skillPoints();
    }

    public PlayerHistory reason(String reason) {

        return data(REASON, reason);
    }

    public PlayerHistory data(String key, Object value) {

        data.put(key, value);
        return this;
    }
}
