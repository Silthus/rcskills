package de.raidcraft.skills;

import de.raidcraft.skills.entities.ConfiguredSkill;
import de.raidcraft.skills.entities.DataStore;
import de.raidcraft.skills.entities.PlayerSkill;
import de.raidcraft.skills.entities.SkilledPlayer;
import de.raidcraft.skills.util.TimeUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.function.Consumer;

public interface SkillContext {

    /**
     * Gets the player skill that is holding this skill context.
     * <p>Use it to retrieve additional properties about the player and his skills.
     *
     * @return the player skill that loaded this skill
     */
    PlayerSkill playerSkill();

    /**
     * Gets the skill that this context wraps.
     *
     * @return the skill of this context
     */
    Skill get();

    /**
     * @return true if the skill is executable and implements {@link Executable}
     */
    default boolean executable() {

        return get() instanceof Executable;
    }

    /**
     * Reloads the skill context disabling it, reloading the config and enabling it again.
     * <p>If anything goes wrong while reloading the skill will stay disabled.
     */
    void reload();

    /**
     * Enables the skill context applying any effects of the skill
     * to the player that is associated with this context.
     * <p>The context will also start any timed tasks the skill requires.
     * <p>Nothing will happen if the skill is already enabled.
     */
    void enable();

    /**
     * Disables the skill context removing any effects of the skill
     * from the player.
     * <p>Nothing will happen if the skill context is not enabled.
     */
    void disable();

    /**
     * Executes this skill if it is an executable skill and if the player is online.
     *
     * @param callback the callback that is called when the skill executed or failed
     */
    void execute(Consumer<ExecutionResult> callback);

    /**
     * Checks if the skill is on cooldown.
     * <p>Will return false if the skill has no configured cooldown.
     *
     * @return true if the skill is on cooldown and cannot be excuted
     */
    default boolean isOnCooldown() {

        return getRemainingCooldown() > 0;
    }

    /**
     * Gets the remaining cooldown of the skill in milliseconds.
     * <p>Use the {@link TimeUtil#formatTime(long)} method to get a pretty formatted time output.
     * <p>The cooldown will be -1 if no cooldown is configured.
     *
     * @return the remaining cooldown of the skill or -1 if no cooldown is configured
     */
    long getRemainingCooldown();

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
     * Gets the data store that is attached to the player skill.
     * <p>Use it to store persistent data for your skill.
     * The store is always scope to the single instance of this skill
     * and player. This means you do not have to worry about storing player ids
     * or mapping between different skills.
     * <p>Make sure you call {@link DataStore#save()} after storing your data.
     *
     * @return the data store for this skill
     */
    default DataStore store() {

        return playerSkill().data();
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

        return player.getUniqueId().equals(skilledPlayer().id());
    }

    /**
     * This is a shortcut to the {@link #applicable(OfflinePlayer)} method
     * inverting the result.
     *
     * @param player the player to check. can be null.
     * @return true if the player is null or not the player of this skill
     */
    default boolean notApplicable(OfflinePlayer player) {

        if (player == null) return true;

        return !applicable(player);
    }
}
