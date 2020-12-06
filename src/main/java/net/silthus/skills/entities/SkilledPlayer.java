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
import java.util.Optional;
import java.util.UUID;

@Entity
@Accessors(fluent = true)
@Getter
@Setter
@Table(name = "rcs_players")
public class SkilledPlayer extends BaseEntity {

    public static final Finder<UUID, SkilledPlayer> find = new Finder<>(SkilledPlayer.class);

    private String name;
    private long level;
    private long exp;
    private long skillPoints;
    @OneToMany(cascade = CascadeType.REMOVE)
    private List<PlayerSkill> skills = new ArrayList<>();

    public SkilledPlayer(OfflinePlayer player) {

        id(player.getUniqueId());
        name(player.getName());
    }

    public OfflinePlayer getOfflinePlayer() {

        return Bukkit.getOfflinePlayer(id());
    }

    public Optional<Player> getBukkitPlayer() {
        return Optional.ofNullable(Bukkit.getPlayer(id()));
    }

    public AddSkillResult addSkill(ConfiguredSkill skill) {

        return addSkill(skill, false);
    }

    public AddSkillResult addSkill(ConfiguredSkill skill, boolean bypassChecks) {

        if (hasSkill(skill)) {
            return new AddSkillResult(skill, this, TestResult.ofSuccess(), false, bypassChecks, name() + " already has the " + skill.alias() + " skill.");
        }

        TestResult testResult = skill.test(this);

        if (testResult.success() || bypassChecks) {
            skills.add(new PlayerSkill(this, skill));
            skill.apply(this);
            save();
            return new AddSkillResult(skill, this, testResult, true, bypassChecks);
        }

        return new AddSkillResult(skill, this, testResult, false, bypassChecks, "Requirements for obtaining the skill " + skill.alias() + " were not met.");
    }

    public Optional<PlayerSkill> getSkill(String alias) {

        return skills().stream()
                .filter(playerSkill -> playerSkill.skill().alias().equalsIgnoreCase(alias))
                .findFirst();
    }

    public Optional<PlayerSkill> getSkill(ConfiguredSkill skill) {

        return skills().stream()
                .filter(playerSkill -> playerSkill.skill().equals(skill))
                .findFirst();
    }

    public void removeSkill(ConfiguredSkill skill) {

        if (!hasSkill(skill)) {
            return;
        }

        getSkill(skill).ifPresent(Model::delete);
    }

    public boolean hasSkill(ConfiguredSkill skill) {

        return getSkill(skill).map(PlayerSkill::active).orElse(false);
    }

    public boolean hasSkill(String alias) {

        return getSkill(alias).map(PlayerSkill::active).orElse(false);
    }
}
