package net.silthus.skills.entities;

import io.ebean.Finder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import net.silthus.skills.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.Instant;
import java.util.*;

@Entity
@Getter
@Setter
@Table(name = "rcs_player_skills")
@Accessors(fluent = true)
public class PlayerSkill extends BaseEntity {

    public static final Finder<UUID, PlayerSkill> find = new Finder<>(PlayerSkill.class);

    @ManyToOne
    private SkilledPlayer player;
    private String identifier;
    private String name;
    private String description;
    private Instant unlocked = null;
    private boolean active = false;

    public PlayerSkill() {

    }

    public PlayerSkill(SkilledPlayer player, ConfiguredSkill skill) {
        this.player = player;
        this.identifier = skill.identifier();
        this.name = skill.name();
        this.description = skill.description();
        save();
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
