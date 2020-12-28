package de.raidcraft.skills;

/**
 * Marks a skill as an executable (active) skill.
 * <p>The {@link #execute(ExecutionContext)} method will be called everytime the skill
 * is used by its owner. Any cooldown, warmup times or other checks
 * will be performed before the method is called.
 * <p>The {@link ExecutionContext} holds all information about the caller context of the skill.
 * <p>Use the {@link ExecutionResult} static methods to create an execution result.
 */
public interface Executable {

    /**
     * @see ExecutionResult#success(ExecutionContext)
     */
    default ExecutionResult success(ExecutionContext context) {
        return ExecutionResult.success(context);
    }

    /**
     * @see ExecutionResult#failure(ExecutionContext, String...)
     */
    default ExecutionResult failure(ExecutionContext context, String... errors) {
        return ExecutionResult.failure(context, errors);
    }

    /**
     * @see ExecutionResult#of(ExecutionContext, boolean, String...)
     */
    default ExecutionResult resultOf(ExecutionContext context, boolean success, String... errors) {
        return ExecutionResult.of(context, success, errors);
    }

    /**
     * Executes this skill and returns a failure or success result based on the outcome.
     * <p>All checks will be performed before this method is called
     * and all costs will be subtracted after this method depending on the result.
     * <p>Use the {@link ExecutionContext} to access all relevant information of the execution.
     * Like parameters, config values or how the skill was executed.
     *
     * @param context the context that executes this skill
     * @throws Exception if any exception occurs during the execution
     * @return the result of the execution
     */
    ExecutionResult execute(ExecutionContext context) throws Exception;
}
