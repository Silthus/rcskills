package net.silthus.skills.entities;

import io.ebean.Finder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import net.silthus.skills.Requirement;
import net.silthus.skills.Skill;
import net.silthus.skills.SkillManager;
import net.silthus.skills.SkillsPlugin;
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
public class PlayerSkill extends BaseEntity implements Skill {

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

    public PlayerSkill(SkilledPlayer player, Skill skill) {
        this.player = player;
        this.identifier = skill.identifier();
        this.name = skill.name();
        this.description = skill.description();
        save();
    }

    @Override
    public void unlock(Player player) {
        if (unlocked != null) return;
        unlocked = Instant.now();
        save();
    }

    public Optional<Skill> skill() {

        return JavaPlugin.getPlugin(SkillsPlugin.class).getSkillManager().getSkill(identifier());
    }

    @Override
    public Collection<Requirement> requirements() {
        return skill().map(Skill::requirements).orElseGet(ArrayList::new);
    }

    public boolean unlocked() {
        return this.unlocked != null;
    }

    @Override
    public void addRequirement(Requirement requirement) {
        skill().ifPresent(skill -> skill.addRequirement(requirement));
    }

    @Override
    public void addRequirements(Collection<Requirement> requirements) {
        skill().ifPresent(skill -> skill.addRequirements(requirements));
    }

    @Override
    public Skill load(ConfigurationSection config) {
        return skill().map(skill -> skill.load(config)).orElse(this);
    }

    @Override
    public void apply(Player player) {
        skill().ifPresent(skill -> skill.apply(player));
    }

    @Override
    public void remove(Player player) {
        skill().ifPresent(skill -> skill.remove(player));
    }
}
