package net.silthus.skills.entities;

import io.ebean.Finder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import net.silthus.skills.events.SetPlayerExpEvent;
import net.silthus.skills.events.SetPlayerLevelEvent;
import net.silthus.skills.events.SetPlayerSkillPointsEvent;
import org.bukkit.Bukkit;

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
    private int level = 1;
    private long totalExp = 0;
    private int skillPoints = 0;

    private PlayerLevel(SkilledPlayer player) {

        this.player = player;
    }

    public PlayerLevel level(int level) {

        SetPlayerLevelEvent event = new SetPlayerLevelEvent(this, this.level, level);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return this;

        this.level = event.getNewLevel();
        return this;
    }

    public PlayerLevel addLevel(int level) {

        return this.level(this.level += level);
    }

    public PlayerLevel exp(long exp) {

        SetPlayerExpEvent event = new SetPlayerExpEvent(this, this.totalExp, exp);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return this;

        this.totalExp = event.getNewExp();
        return this;
    }

    public PlayerLevel addExp(long exp) {

        return this.exp(totalExp += exp);
    }

    public PlayerLevel skillPoints(int skillPoints) {

        SetPlayerSkillPointsEvent event = new SetPlayerSkillPointsEvent(this, this.skillPoints, skillPoints);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return this;

        this.skillPoints = event.getNewSkillPoints();
        return this;
    }

    public PlayerLevel addSkillPoints(int skillPoints) {

        return this.skillPoints(this.skillPoints += skillPoints);
    }
}
