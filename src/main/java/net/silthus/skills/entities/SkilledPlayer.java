package net.silthus.skills.entities;

import io.ebean.Finder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import net.silthus.skills.AddSkillResult;
import net.silthus.skills.Skill;
import net.silthus.skills.SkillManager;
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
@Table(name = "skills_players")
public class SkilledPlayer extends BaseEntity {

    public static final Finder<UUID, SkilledPlayer> find = new Finder<>(SkilledPlayer.class);

    private String name;
    @OneToMany(cascade = CascadeType.REMOVE)
    private List<PlayerSkill> playerSkills = new ArrayList<>();

    public SkilledPlayer(OfflinePlayer player) {

        id(player.getUniqueId());
        name(player.getName());
    }

    public void loadSkills() {

        Player player = Bukkit.getPlayer(id());
        if (player == null) return;
        for (PlayerSkill playerSkill : playerSkills()) {
            SkillManager.instance().getSkill(playerSkill.identifier())
                    .ifPresent(skill -> skill.apply(player));
        }
    }

    public void addSkill(Skill skill) {

        addSkill(skill, false);
    }

    public AddSkillResult addSkill(Skill skill, boolean bypassChecks) {

        if (hasSkill(skill)) {
            return new AddSkillResult(skill, this, TestResult.ofSuccess(), false, bypassChecks, name() + " already has the " + skill.identifier() + " skill.");
        }

        Player player = Bukkit.getPlayer(id());
        TestResult testResult = skill.test(player);

        if (testResult.success() || bypassChecks) {
            playerSkills.add(new PlayerSkill(this, skill.identifier()));
            skill.apply(player);
            return new AddSkillResult(skill, this, testResult, true, bypassChecks);
        }

        return new AddSkillResult(skill, this, testResult, false, bypassChecks, "Requirements for obtaining the skill " + skill.identifier() + " were not met.");
    }

    public boolean hasSkill(Skill skill) {

        return hasSkill(skill.identifier());
    }

    public boolean hasSkill(String identifier) {

        return playerSkills().stream()
                .filter(PlayerSkill::unlocked)
                .anyMatch(playerSkill -> playerSkill.identifier().equals(identifier));
    }
}
