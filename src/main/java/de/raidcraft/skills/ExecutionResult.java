package de.raidcraft.skills;

import de.raidcraft.skills.util.TimeUtil;

import java.util.Collection;

/**
 * The result of the skill execution.
 * <p>Costs for the execution of the skill will only be applied
 * if the execution result was successful.
 */
public interface ExecutionResult {

    /**
     * Creates a successful execution result for the given context.
     *
     * @param context the context that was executed
     * @return the final result of the execution
     */
    static ExecutionResult success(ExecutionContext context) {

        return new DefaultExecutionResult(context);
    }

    /**
     * Creates a failed execution result for the given context.
     * <p>Pass in any errors that should be forwarded to the end user.
     *
     * @param context the context of the execution
     * @param errors the errors that occurred during the execution
     * @return the final result of the execution
     */
    static ExecutionResult failure(ExecutionContext context, String... errors) {

        return new DefaultExecutionResult(context, Status.FAILURE, errors);
    }

    /**
     * Creates a delayed execution result representing the state of the skill.
     *
     * @param context the context of the execution
     * @return the created result
     */
    static ExecutionResult delayed(ExecutionContext context) {

        return new DefaultExecutionResult(context, Status.DELAYED);
    }

    /**
     * Creates an execution result that marks the skill as on cooldown.
     * <p>The result will be treated as a failure.
     *
     * @param context the context of the execution
     * @return the created result
     */
    static ExecutionResult cooldown(ExecutionContext context) {

        return new DefaultExecutionResult(context, Status.COOLDOWN);
    }

    /**
     * Creates a new execution result with the given status.
     *
     * @param context the context of the execution
     * @param status the status
     * @param errors any error that occurred
     * @return the created result
     */
    static ExecutionResult of(ExecutionContext context, Status status, String... errors) {

        return new DefaultExecutionResult(context, status, errors);
    }

    static ExecutionResult exception(ExecutionContext context, Throwable e) {

        return new DefaultExecutionResult(context, e);
    }

    /**
     * Creates a new successful or failed execution result depending on the provided boolean flag.
     * <p>Pass in any errors that might occur if the outcome is negative.
     *
     * @param context the context of the execution
     * @param success the outcome of the execution
     * @param errors any errors that might occur
     * @return the final result of the execution
     */
    static ExecutionResult of(ExecutionContext context, boolean success, String... errors) {

        return new DefaultExecutionResult(context, success, errors);
    }

    /**
     * @return the context that was executing the skill or effect
     */
    ExecutionContext context();

    /**
     * Returns a list of errors that occurred during the execution.
     * <p>The list is usually empty if the execution was a success,
     * but this is not enforced.
     *
     * @return a list of errors that occurred during execution
     */
    Collection<String> errors();

    /**
     * Gets the current status of the skill result.
     * <p>Check if the skill execution as {@link #delayed()},
     * a {@link #success()} or {@link #failure()}.
     *
     * @return the status of the skill execution
     */
    Status status();

    /**
     * @return true if the skill execution succeeded.
     */
    default boolean success() {

        return status() == Status.SUCCESS;
    }

    /**
     * @return true if the skill execution failed, throws an exception or the skill is on cooldown
     */
    default boolean failure() {

        return status() == Status.FAILURE || status() == Status.EXCEPTION || status() == Status.COOLDOWN;
    }

    /**
     * Returns any exception that was thrown during the execution or null if none.
     * <p>The exception is never null if the status is {@link Status#EXCEPTION}.
     *
     * @return the thrown exception or null
     */
    Throwable exception();

    /**
     * @return true if the execution of the skill is delayed due to warmup or a delay
     */
    default boolean delayed() {

        return status() == Status.DELAYED || status() == Status.WARMUP;
    }

    default boolean cooldown() {

        return status() == Status.COOLDOWN;
    }

    default long remainingCooldown() {

        return context().source().getRemainingCooldown();
    }

    default String formattedCooldown() {

        return TimeUtil.formatTime(remainingCooldown());
    }

    default String formattedDelay() {

        return TimeUtil.formatTime(TimeUtil.ticksToMillis(context().config().delay()));
    }

    enum Status {
        SUCCESS,
        FAILURE,
        EXCEPTION,
        DELAYED,
        WARMUP,
        COOLDOWN
    }
}
