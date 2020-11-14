package net.silthus.skills.entities;

import io.ebean.Finder;
import io.ebean.Model;
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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Accessors(fluent = true)
@Getter
@Setter
@Table(name = "rcs_players")
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
            skills.add(new PlayerSkill(this, skill));
            skill.apply(player);
            save();
            return new AddSkillResult(skill, this, testResult, true, bypassChecks);
        }

        return new AddSkillResult(skill, this, testResult, false, bypassChecks, "Requirements for obtaining the skill " + skill.identifier() + " were not met.");
    }

    @Override
    public void removeSkill(Skill skill) {

        if (!hasSkill(skill.identifier())) {
            return;
        }

        skills().stream()
                .filter(playerSkill -> playerSkill.identifier().equals(skill.identifier()))
                .findFirst().ifPresent(Model::delete);

    }

    @Override
    public boolean hasSkill(Skill skill) {

        return hasSkill(skill.identifier());
    }

    @Override
    public boolean hasSkill(String identifier) {

        return this.skills.stream()
                .filter(PlayerSkill::unlocked)
                .anyMatch(playerSkill -> playerSkill.identifier().equalsIgnoreCase(identifier));
    }
}
