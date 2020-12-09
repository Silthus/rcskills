package net.silthus.skills.entities;

import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.Transactional;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import net.silthus.skills.AddSkillResult;
import net.silthus.skills.TestResult;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.persistence.*;
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

    /**
     * Gets an existing player from the database or creates a new record from the given player.
     * <p>This method takes an {@link OfflinePlayer} for easier access to skills while players are offline.
     * However the skill can only be applied to the player if he is online. Any interaction will fail silently while offline.
     *
     * @param player the player that should be retrieved or created
     * @return a skilled player from the database
     */
    public static SkilledPlayer getOrCreate(OfflinePlayer player) {

        return Optional.ofNullable(find.byId(player.getUniqueId()))
                .orElseGet(() -> {
                    SkilledPlayer skilledPlayer = new SkilledPlayer(player);
                    skilledPlayer.save();
                    return skilledPlayer;
                });
    }

    private String name;

    @OneToOne(cascade = CascadeType.ALL)
    private PlayerLevel level;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "player")
    private List<PlayerSkill> skills = new ArrayList<>();

    private SkilledPlayer(OfflinePlayer player) {

        id(player.getUniqueId());
        name(player.getName());
        level(PlayerLevel.getOrCreate(this));
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

    @Transactional
    public AddSkillResult addSkill(ConfiguredSkill skill, boolean bypassChecks) {

        if (hasSkill(skill)) {
            return new AddSkillResult(skill, this, TestResult.ofSuccess(), false, bypassChecks, name() + " already has the " + skill.alias() + " skill.");
        }

        TestResult testResult = skill.test(this);

        if (testResult.success() || bypassChecks) {
            skills.add(PlayerSkill.getOrCreate(this, skill));
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

        return getSkill(skill).isPresent();
    }

    public boolean hasSkill(String alias) {

        return getSkill(alias).isPresent();
    }
}
