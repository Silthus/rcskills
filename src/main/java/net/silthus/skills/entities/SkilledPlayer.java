package net.silthus.skills.entities;

import io.ebean.Finder;
import io.ebean.annotation.Transactional;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import net.silthus.skills.actions.AddSkillAction;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

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
                    skilledPlayer.level(PlayerLevel.getOrCreate(skilledPlayer));
                    skilledPlayer.save();
                    return skilledPlayer;
                });
    }

    private String name;

    @OneToOne(cascade = CascadeType.ALL)
    private PlayerLevel level;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "player")
    private Set<PlayerSkill> skills = new HashSet<>();

    @OneToMany(cascade = CascadeType.REMOVE)
    private List<PlayerHistory> history = new ArrayList<>();

    private SkilledPlayer(OfflinePlayer player) {

        id(player.getUniqueId());
        name(player.getName());
    }

    public OfflinePlayer getOfflinePlayer() {

        return Bukkit.getOfflinePlayer(id());
    }

    public Optional<Player> getBukkitPlayer() {
        return Optional.ofNullable(Bukkit.getPlayer(id()));
    }

    public AddSkillAction.Result addSkill(ConfiguredSkill skill) {

        return addSkill(skill, false);
    }

    @Transactional
    public AddSkillAction.Result addSkill(ConfiguredSkill skill, boolean bypassChecks) {

        return new AddSkillAction(this, skill).execute(bypassChecks);
    }

    public Optional<PlayerSkill> getSkill(String alias) {

        return ConfiguredSkill.findByAliasOrName(alias)
                .map(this::getSkill);
    }

    public PlayerSkill getSkill(ConfiguredSkill skill) {

        return PlayerSkill.getOrCreate(this, skill);
    }

    public PlayerSkill removeSkill(ConfiguredSkill skill) {

        PlayerSkill playerSkill = getSkill(skill);
        playerSkill.delete();
        return playerSkill;
    }

    public boolean hasActiveSkill(ConfiguredSkill skill) {

        return getSkill(skill).active();
    }

    public boolean hasSkill(ConfiguredSkill skill) {

        return getSkill(skill).unlocked();
    }

    public boolean hasSkill(String alias) {

        return getSkill(alias).map(PlayerSkill::unlocked).orElse(false);
    }

    public Collection<PlayerSkill> unlockedSkills() {

        return skills().stream()
                .filter(PlayerSkill::unlocked)
                .collect(Collectors.toList());
    }
}
