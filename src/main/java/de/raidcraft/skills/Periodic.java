package de.raidcraft.skills;

/**
 * Marks the skill as a periodic skill ticking in the configured interval.
 * <p>There is also an async version of this called {@link PeriodicAsync}.
 */
public interface Periodic {

    /**
     * The tick method is called as defined by the configuration interval.
     * <p>The tick call is synchronized and allows you to access everything.
     */
    void tick();
}
