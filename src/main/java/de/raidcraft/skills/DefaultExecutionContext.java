package de.raidcraft.skills;

import lombok.Value;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;

@Value
@Accessors(fluent = true)
class DefaultExecutionContext implements ExecutionContext {

    SkillContext source;
    Skill skill;
    Consumer<ExecutionResult> callback;
    ExecutionConfig config;

    DefaultExecutionContext(SkillContext source, Consumer<ExecutionResult> callback) {
        this.source = source;
        this.skill = source.get();
        this.callback = callback;
        this.config = source.configuredSkill().executionConfig();
    }

    @Override
    public <TTarget> Optional<TTarget> target(Class<TTarget> targetClass) {

        return SkillsPlugin.instance().getTargetManager().resolve(this, targetClass);
    }

    @Override
    public void run() {

        if (skill instanceof Executable) {
            try {
                ((Executable) skill).execute(this);
                source().playerSkill().lastUsed(Instant.now()).save();
                callback.accept(ExecutionResult.success(this));
            } catch (Exception e) {
                callback.accept(ExecutionResult.exception(this, e));
            }
        }
    }
}
