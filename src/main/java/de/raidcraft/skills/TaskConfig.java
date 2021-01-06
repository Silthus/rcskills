package de.raidcraft.skills;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import org.bukkit.configuration.ConfigurationSection;

@Value
@Log(topic = "RCSkills")
@Accessors(fluent = true)
public class TaskConfig {

    @Getter(AccessLevel.NONE)
    ConfigurationSection config;

    public TaskConfig(ConfigurationSection config) {

        this.config = config;
    }

    public long interval() {

        return config.getLong("interval", 20L);
    }

    public long interval(long defaultInterval) {

        return config.getLong("interval", defaultInterval);
    }
}
