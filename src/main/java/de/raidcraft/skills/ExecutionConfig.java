package de.raidcraft.skills;

import de.raidcraft.skills.util.TimeUtil;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

@Value
@Builder
@AllArgsConstructor
@Log(topic = "RCSkills")
@Accessors(fluent = true)
public class ExecutionConfig {

    @Getter(AccessLevel.NONE)
    ConfigurationSection config;
    /**
     * The range in blocks of this skill.
     */
    int range;
    /**
     * The cooldown in ticks.
     */
    long cooldown;
    /**
     * The warmup time in ticks where the player is not allowed to move.
     */
    long warmup;
    /**
     * The delay in ticks before the skill is executed.
     */
    long delay;

    public ExecutionConfig(ConfigurationSection config) {
        this.config = config;
        this.range = config.getInt("range", 30);
        cooldown = TimeUtil.parseTimeAsMilliseconds(config.getString("cooldown", "0"));
        warmup = TimeUtil.parseTimeAsTicks(config.getString("warmup", "0"));
        delay = TimeUtil.parseTimeAsTicks(config.getString("delay", "0"));
    }

    @SuppressWarnings("unchecked")
    public <TValue> TValue get(String key, TValue defaultValue) {

        try {
            return (TValue) defaultValue.getClass().cast(config.get(key, defaultValue));
        } catch (ClassCastException e) {
            log.warning("unable to convert execution context config value " + key + " to type: " + defaultValue.getClass().getCanonicalName());
            return defaultValue;
        }
    }
}
