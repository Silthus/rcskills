package de.raidcraft.skills;

import lombok.Value;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;

@Value
@Accessors(fluent = true)
class DefaultExecutionResult implements ExecutionResult {

    ExecutionContext context;
    Status status;
    Set<String> errors;
    Throwable exception;

    public DefaultExecutionResult(ExecutionContext context) {
        this.context = context;
        this.status = Status.SUCCESS;
        this.errors = new HashSet<>();
        this.exception = null;
    }

    public DefaultExecutionResult(ExecutionContext context, boolean success, String... errors) {
        this.context = context;
        this.status = success ? Status.SUCCESS : Status.FAILURE;
        this.errors = Set.of(errors);
        this.exception = null;
    }

    public DefaultExecutionResult(ExecutionContext context, Status status, String... errors) {
        this.context = context;
        this.status = status;
        this.errors = Set.of(errors);
        this.exception = null;
    }

    public DefaultExecutionResult(ExecutionContext context, Throwable throwable) {
        this.context = context;
        this.status = Status.EXCEPTION;
        this.errors = Set.of(throwable.getMessage());
        this.exception = throwable;
    }
}
