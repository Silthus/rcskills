package de.raidcraft.skills;

import de.raidcraft.skills.entities.ConfiguredSkill;
import de.raidcraft.skills.entities.PlayerSkill;
import de.raidcraft.skills.entities.SkilledPlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;

public interface SkillContext {

    /**
     * Gets the player skill that is holding this skill context.
     * <p>Use it to retrieve additional properties about the player and his skills.
     *
     * @return the player skill that loaded this skill
     */
    PlayerSkill playerSkill();

    /**
     * Gets the player that owns this skill.
     *
     * @return the player of this skill
     */
    default SkilledPlayer skilledPlayer() {

        return playerSkill().player();
    }

    /**
     * Gets the base configuration of this skill.
     *
     * @return the configured skill
     */
    default ConfiguredSkill configuredSkill() {

        return playerSkill().configuredSkill();
    }

    /**
     * Gets the associated offline player of this skill.
     *
     * @return the offline player of this skill
     */
    default OfflinePlayer offlinePlayer() {

        return skilledPlayer().offlinePlayer();
    }

    /**
     * Gets the bukkit player of this skill if he is online.
     *
     * @return the online player of this skill
     */
    default Optional<Player> player() {

        return skilledPlayer().bukkitPlayer();
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

        return offlinePlayer().equals(player);
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
}
