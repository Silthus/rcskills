package de.raidcraft.skills;

import lombok.Value;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;

@Value
@Accessors(fluent = true)
class DefaultExecutionResult implements ExecutionResult {

    ExecutionContext context;
    boolean success;
    Set<String> errors;

    public DefaultExecutionResult(ExecutionContext context) {
        this.context = context;
        this.success = true;
        this.errors = new HashSet<>();
    }

    public DefaultExecutionResult(ExecutionContext context, boolean success) {
        this.context = context;
        this.success = success;
        this.errors = new HashSet<>();
    }

    public DefaultExecutionResult(ExecutionContext context, boolean success, String... errors) {
        this.context = context;
        this.success = success;
        this.errors = Set.of(errors);
    }

    public DefaultExecutionResult(ExecutionContext context, String... errors) {
        this.context = context;
        this.success = false;
        this.errors = Set.of(errors);
    }
}
