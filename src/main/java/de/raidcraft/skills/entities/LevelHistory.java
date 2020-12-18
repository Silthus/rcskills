package de.raidcraft.skills.entities;

import io.ebean.annotation.DbJson;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@Setter
@Accessors(fluent = true)
@Table(name = "rcs_level_history")
public class LevelHistory extends BaseEntity {

    public static final String REASON = "reason";

    public static LevelHistory create(Level level) {

        return new LevelHistory(level);
    }

    @ManyToOne
    private Level level;
    private int oldLevel;
    private int newLevel;
    private long oldExp;
    private long newExp;
    @DbJson
    private Map<String, Object> data = new HashMap<>();

    LevelHistory(Level level) {

        this.level = level;
        this.oldLevel = level.getLevel();
        this.newLevel = level.getLevel();
        this.oldExp = level.getTotalExp();
        this.newExp = level.getTotalExp();
    }

    public LevelHistory reason(String reason) {

        return data(REASON, reason);
    }

    public LevelHistory data(String key, Object value) {

        data.put(key, value);
        return this;
    }
}
