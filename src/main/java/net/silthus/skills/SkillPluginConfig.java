package net.silthus.skills;

import de.exlll.configlib.annotation.Comment;
import de.exlll.configlib.annotation.ConfigurationElement;
import de.exlll.configlib.configs.yaml.BukkitYamlConfiguration;
import de.exlll.configlib.format.FieldNameFormatters;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;

@Getter
@Setter
public class SkillPluginConfig extends BukkitYamlConfiguration {

    @Comment("The relative path where your skill configs are located.")
    private String skillsPath = "skills";
    @Comment("Set to false if you want to disable broadcasting players leveling up to everyone.")
    private boolean broadcastLevelup = true;
    private DatabaseConfig database = new DatabaseConfig();
    @Comment("Define the expression that calculates the required exp for each level here.")
    private LevelConfig levelConfig = new LevelConfig();

    public SkillPluginConfig(Path path) {

        super(path, BukkitYamlProperties.builder().setFormatter(FieldNameFormatters.LOWER_UNDERSCORE).build());
    }

    @ConfigurationElement
    @Getter
    @Setter
    public static class DatabaseConfig {

        private String username = "sa";
        private String password = "sa";
        private String driver = "h2";
        private String url = "jdbc:h2:~/skills.db";
    }

    @ConfigurationElement
    @Getter
    @Setter
    public static class LevelConfig {

        private int maxLevel = 100;
        @Comment({
                "You can use any of the following variables and all java Math.* expressions: ",
                "  - x: settable in this config",
                "  - y: settable in this config",
                "  - z: settable in this config",
                "  - level: the current level of the player"
        })
        private String expToNextLevel = "(-0.4 * Math.pow(level, 2)) + (x * Math.pow(level, 2))";
        private double x = 10.4;
        private double y = 0;
        private double z = 0;
    }
}
