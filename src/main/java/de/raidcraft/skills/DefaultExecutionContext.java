package de.raidcraft.skills;

import lombok.Value;
import lombok.experimental.Accessors;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Optional;

@Value
@Accessors(fluent = true)
class DefaultExecutionContext implements ExecutionContext {

    SkillContext source;
    ExecutionConfig config;

    DefaultExecutionContext(SkillContext source) {
        this.source = source;
        ConfigurationSection config = source.configuredSkill().getConfig();
        ConfigurationSection section = config.getConfigurationSection("execution");
        this.config = new ExecutionConfig(section == null ? config.createSection("execution") : section);
    }

    @Override
    public <TTarget> Optional<TTarget> target(Class<TTarget> targetClass) {

        return SkillsPlugin.instance().getTargetManager().resolve(this, targetClass);
    }
}
