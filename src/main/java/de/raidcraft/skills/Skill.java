package de.raidcraft.skills;

import de.raidcraft.skills.entities.ConfiguredSkill;
import de.raidcraft.skills.entities.PlayerSkill;
import de.raidcraft.skills.entities.SkilledPlayer;
import lombok.Value;
import lombok.experimental.Accessors;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.function.Function;

public interface Skill {

    /**
     * Gets the player skill that is attached to the instance of this skill.
     * <p>Use it to retrive additional properties about the player and his skills.
     *
     * @return the player skill that loaded this skill
     */
    PlayerSkill getPlayerSkill();

    /**
     * Gets the player that owns this skill.
     *
     * @return the player of this skill
     */
    default SkilledPlayer getSkilledPlayer() {

        return getPlayerSkill().player();
    }

    /**
     * Gets the base configuration of this skill.
     *
     * @return the configured skill
     */
    default ConfiguredSkill getConfiguredSkill() {

        return getPlayerSkill().configuredSkill();
    }

    /**
     * Gets the associated offline player of this skill.
     *
     * @return the offline player of this skill
     */
    default OfflinePlayer getOfflinePlayer() {

        return getSkilledPlayer().getOfflinePlayer();
    }

    /**
     * Gets the bukkit player of this skill if he is online.
     *
     * @return the online player of this skill
     */
    default Optional<Player> getPlayer() {

        return getSkilledPlayer().getBukkitPlayer();
    }

    /**
     * Checks if this skill is applicable to the given player.
     * <p>This will check if the given player is the same as the skilled player.
     *
     * @param player the player to check. can be null.
     * @return false if the player is null or not the same as the skilled player of this skill
     */
    default boolean applicable(OfflinePlayer player) {

        if (player == null) return false;

        return getOfflinePlayer().equals(player);
    }

    /**
     * This is a shortcut to the {@link #applicable(OfflinePlayer)} method
     * inverting the result.
     *
     * @param player the player to check. can be null.
     * @return true if the player is null or not the player of this skill
     */
    default boolean notApplicable(OfflinePlayer player) {

        return !applicable(player);
    }

    /**
     * Load is called with the config of the configured skill after the creation of this skill.
     * <p>Use it to load your skill specific configuration.
     * <p>You can use the {@link net.silthus.configmapper.ConfigOption} annotations as an alternative.
     *
     * @param config the config to load the skill with
     */
    default void load(ConfigurationSection config) {}

    /**
     * Applies the effects of this skill to the given player.
     * <p>The apply method is called everytime a player logs on
     * or obtains the skill.
     */
    default void apply() {}

    /**
     * Removes any effects of the skill from the given player.
     * <p>remove(...) is called everytime the skill becomes inactive and
     * when the player loses access to it, e.g. when logging out.
     */
    default void remove() {}

    @Value
    @Accessors(fluent = true)
    class Registration<TSkill extends Skill> {

        String type;
        Class<TSkill> skillClass;
        Function<PlayerSkill, TSkill> supplier;
    }
}
