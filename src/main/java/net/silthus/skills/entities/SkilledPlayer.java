package net.silthus.skills.entities;

import io.ebean.Finder;
import io.ebean.annotation.Transactional;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import net.silthus.skills.actions.AddSkillAction;
import net.silthus.skills.events.SetPlayerExpEvent;
import net.silthus.skills.events.SetPlayerLevelEvent;
import net.silthus.skills.events.SetPlayerSkillPointsEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
                    skilledPlayer.insert();
                    return skilledPlayer;
                });
    }

    private String name;
    private int skillPoints = 0;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private Level level = new Level();

    @OneToMany(cascade = CascadeType.REMOVE)
    private Set<PlayerSkill> skills = new HashSet<>();

    SkilledPlayer(OfflinePlayer player) {

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

    public SkilledPlayer setLevel(int level) {

        Level playerLevel = level();
        SetPlayerLevelEvent event = new SetPlayerLevelEvent(this, playerLevel.getLevel(), level, playerLevel.getTotalExp());
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return this;

        playerLevel.setLevel(event.getNewLevel());

        if (event.getExp() != playerLevel.getTotalExp()) {
            playerLevel.setTotalExp(event.getExp());
        }

        return this;
    }

    public SkilledPlayer addLevel(int level) {

        return setLevel(this.level.getLevel() + level);
    }

    public SkilledPlayer setExp(long exp) {

        return setExp(exp, null);
    }

    public SkilledPlayer setExp(long exp, String reason) {

        SetPlayerExpEvent event = new SetPlayerExpEvent(this, level.getTotalExp(), exp, level.getLevel(), reason);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return this;

        level.setTotalExp(event.getNewExp());
        if (event.getLevel() != level.getLevel()) {
            level.setLevel(event.getLevel());
        }
        return this;
    }

    public SkilledPlayer addExp(long exp, String reason) {

        return this.setExp(level().getTotalExp() + exp, reason);
    }

    public SkilledPlayer setSkillPoints(int skillPoints) {

        SetPlayerSkillPointsEvent event = new SetPlayerSkillPointsEvent(this, this.skillPoints, skillPoints);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return this;

        this.skillPoints = skillPoints;
        return this;
    }

    public SkilledPlayer addSkillPoints(int skillPoints) {

        return this.setSkillPoints(this.skillPoints + skillPoints);
    }
}
