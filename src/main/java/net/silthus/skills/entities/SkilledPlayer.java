package net.silthus.skills.entities;

import io.ebean.Finder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import net.silthus.skills.AddSkillResult;
import net.silthus.skills.Skill;
import net.silthus.skills.TestResult;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.persistence.*;
import java.util.*;

@Entity
@Accessors(fluent = true)
@Getter
@Setter
@Table(name = "skills_players")
public class SkilledPlayer extends BaseEntity implements net.silthus.skills.SkilledPlayer {

    public static final Finder<UUID, SkilledPlayer> find = new Finder<>(SkilledPlayer.class);

    private String name;
    @OneToMany(cascade = CascadeType.REMOVE)
    private List<PlayerSkill> skills = new ArrayList<>();

    public SkilledPlayer(OfflinePlayer player) {

        id(player.getUniqueId());
        name(player.getName());
    }

    @Override
    public AddSkillResult addSkill(Skill skill) {

        return addSkill(skill, false);
    }

    @Override
    public AddSkillResult addSkill(Skill skill, boolean bypassChecks) {

        if (hasSkill(skill)) {
            return new AddSkillResult(skill, this, TestResult.ofSuccess(), false, bypassChecks, name() + " already has the " + skill.identifier() + " skill.");
        }

        Player player = Bukkit.getPlayer(id());
        TestResult testResult = skill.test(player);

        if (testResult.success() || bypassChecks) {
            skills.add(new PlayerSkill(this, skill.identifier()));
            skill.apply(player);
            return new AddSkillResult(skill, this, testResult, true, bypassChecks);
        }

        return new AddSkillResult(skill, this, testResult, false, bypassChecks, "Requirements for obtaining the skill " + skill.identifier() + " were not met.");
    }

    @Override
    public boolean hasSkill(Skill skill) {

        return hasSkill(skill.identifier());
    }

    @Override
    public boolean hasSkill(String identifier) {

        return skills().stream()
                .filter(PlayerSkill::unlocked)
                .anyMatch(playerSkill -> playerSkill.identifier().equals(identifier));
    }
}
