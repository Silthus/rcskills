package de.raidcraft.skills.util;

import java.util.Random;

/**
 * The PseudoRandomGenerator is used to test an increasing chance based of the initial chance.
 * <p>
 * Use it to align the preceived chance of randomness for users with the actual randomness.
 * <p>
 * The generator will keep track of an iteration counter that will increase with every
 * failed check and increase the chance for the next check. It resets after the chance was hit.
 * <p>
 * By default the base chance is multiplied with the iteration count. You can modify this
 * by adjusting the {@link #multiplier()}. It defaults to 1.0f.
 * <p>
 * The base {@link #chance()} is calculated when creating the pseudo random generator based off
 * the following formula: <pre>chance / ((1.0f / chance) - 1.0f)</pre>
 * The chance cannot not be greater than 1 or less than 0.
 * Any value above or below will be set to the min or max value.
 * <br><p>
 * You can {@link #reset()} the current iteration at any time and calculate the {@link #nextChance()}
 * for the current iteration.
 * <p>However when using this generator your practically only need to create it with your
 * chance and optional multiplier and then call the {@link #hit()} method everytime the chance
 * should be checked. The iteration counter will increase and reset automatically every time {@code hit()} is called.
 * <br><pre>{@code
 * // a chance of 25% will result in an initial base chance of 8.3%
 * // and increase with every failed hit multiplied by the failed iterations
 * // e.g.: after three failed iterations the nextChance() will be 33% and 41,5% after four failed attempts
 * PseudoRandomGenerator random = PseudoRandomGenerator.create(0.25f);
 * if (random.hit()) {
 *     // success
 * }
 * }</pre>
 *
 * @author Michael Reichenbach<michael@reichenbach.in>
 * @since 08-01-2020
 * @version 1
 */
public final class PseudoRandomGenerator {

    /**
     * Creates a new PseudoRandomGenerator with the given chance.
     * <p>The generator will use a default interation multiplier of 1.0f.
     *
     * @param chance the chance of the pseudo random generator as percentage.
     *               A chance greater than {@code 1.0} will be set to {@code 1.0} and less then {@code 0} will become {@code 0}.
     *               e.g.: {@code 0.25f} represents a chance of 25%.
     * @return the created {@link PseudoRandomGenerator} with a fresh {@link Random} seed.
     */
    public static PseudoRandomGenerator create(float chance) {

        return new PseudoRandomGenerator(chance);
    }

    /**
     * Creates a new PseudoRandomGenerator with the given chance using the supplied iteration multiplier.
     *
     * @param chance the chance of the pseudo random generator as percentage.
     *               A chance greater than {@code 1.0} will be set to {@code 1.0} and less then {@code 0} will become {@code 0}.
     *               e.g.: {@code 0.25f} represents a chance of 25%.
     * @param multiplier the multiplier that is multiplied with the iteration count and base chance.
     *                   This defaults to {@code 1.0f} when using the {@link #create(float)} method.
     * @return the created {@link PseudoRandomGenerator} with a fresh {@link Random} seed.
     */
    public static PseudoRandomGenerator create(float chance, float multiplier) {

        return new PseudoRandomGenerator(chance, multiplier);
    }

    private final Random random;
    private final float chance;
    private final float multiplier;

    private int iteration = 0;

    private PseudoRandomGenerator(float chance, float multiplier) {

        this.random = new Random();
        this.chance = calculateBaseChance(chance);
        this.multiplier = multiplier;
    }

    private PseudoRandomGenerator(float chance) {

        this(chance, 1.0f);
    }

    /**
     * Gets the base chance that was intially calculated when creating the generator.
     * <p>Use the {@link #nextChance()} method the retrieve the relative chance based off the current iteration.
     *
     * @return the base chance of this pseudo random generator
     */
    public float chance() {

        return this.chance;
    }

    /**
     * The multiplier is multiplied with the iteration count
     * and base chance to calculate the {@link #nextChance()}.
     *
     * @return the multiplier that calculates the next chance
     */
    public float multiplier() {

        return this.multiplier;
    }

    /**
     * The iteration count is increased on every failure and reset on a success.
     * <p>You can reset it manually by calling {@link #reset()}.
     *
     * @return the current iteration count
     */
    public int iteration() {

        return this.iteration;
    }

    /**
     * Sets the iteration of this generator to the given value.
     * <p>Any value below zero will be set to zero.
     *
     * @param iteration the iteration that this generator is set to
     * @return this generator
     */
    public PseudoRandomGenerator iteration(int iteration) {

        if (iteration < 0) iteration = 0;

        this.iteration = iteration;

        return this;
    }

    /**
     * Calculates the chance for the next iteration.
     * <p>The next chance is calculated like this:
     * <pre>{@code
     *     chance() * ((iteration() + 1) * multiplier())
     * }</pre>
     *
     * @return the chance of the next iteration
     */
    public float nextChance() {

        return chance() * ((iteration() + 1) * multiplier());
    }

    /**
     * Resets the iteration count of this generator back to zero.
     *
     * @return this generator
     */
    public PseudoRandomGenerator reset() {

        return iteration(0);
    }

    /**
     * Hits the generator with a random number and checks if it is below the next chance.
     * <p>Returns true if the check was successful and resets the iteration count.
     * <p>Returns false if the check was a failure and increases the iteration count by one.
     *
     * @return the result of the random chance check against the iteration and base chance
     */
    public boolean hit() {

        if (random.nextFloat() < nextChance()) {
            reset();
            return true;
        }

        iteration++;

        return false;
    }

    private float calculateBaseChance(float chance) {

        if (chance > 1.0) chance = 1.0f;
        if (chance <= 0) chance = 1.0f;
        // lets calculate the base chance by taking the number of times the action should hit
        // e.g. with a chance of 0.25 -> 25% 1/4 should be a hit
        return chance / ((1.0f / chance) - 1.0f);
    }
}
