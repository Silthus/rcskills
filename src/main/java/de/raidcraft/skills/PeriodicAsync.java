package de.raidcraft.skills;

/**
 * Marks the skill as a periodic async skill that ticks in the configured interval.
 * <p>Async operations in Spigot are very limited and you should use this with care.
 * <p>Use cases for this are long running operations, database calls or player messages.
 * <p>Use the {@link Periodic} marker if you need to do anything else,
 * like interactions with the world or player.
 */
public interface PeriodicAsync {

    /**
     * This method is called in the configured interval.
     * Use it to do your stuff in an async thread.
     */
    void tickAsync();
}
