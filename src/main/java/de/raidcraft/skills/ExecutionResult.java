package de.raidcraft.skills;

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

        return new DefaultExecutionResult(context, errors);
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
     * @return true if the skill execution succeeded.
     */
    boolean success();

    /**
     * @return true if the skill execution failed.
     */
    default boolean failure() {

        return !success();
    }
}
