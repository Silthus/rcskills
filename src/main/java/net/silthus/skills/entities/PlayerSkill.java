package net.silthus.skills.entities;

import io.ebean.Finder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "skills_player_skills")
@Accessors(fluent = true)
public class PlayerSkill extends BaseEntity {

    public static final Finder<UUID, PlayerSkill> find = new Finder<>(PlayerSkill.class);

    @ManyToOne
    private SkilledPlayer player;
    private String identifier;
    private boolean unlocked;

    public PlayerSkill(SkilledPlayer player, String identifier) {

        this.player = player;
        this.identifier = identifier;
        this.unlocked = true;
    }
}
