package de.raidcraft.skills;

import de.exlll.configlib.annotation.Comment;
import de.exlll.configlib.annotation.ConfigurationElement;
import de.exlll.configlib.annotation.ElementType;
import de.exlll.configlib.configs.yaml.BukkitYamlConfiguration;
import de.exlll.configlib.format.FieldNameFormatters;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

import java.nio.file.Path;
import java.util.*;

@Getter
@Setter
public class SkillPluginConfig extends BukkitYamlConfiguration {

    private boolean debug = false;
    @Comment("The relative path where your skill configs are located.")
    private String skillsPath = "skills";
    @Comment("The relative path where your skill and effect modules (jar files) are located.")
    private String modulePath = "modules";
    @Comment("Set to true to automatically load skill classes and factories from other plugins.")
    private boolean loadClassesFromPlugins = false;
    @Comment("Set to false if you want to disable broadcasting players leveling up to everyone.")
    private boolean broadcastLevelup = true;
    @Comment("The time in ticks until the /rcs buy command confirmation times out.")
    private long buyCommandTimeout = 600L;
    @Comment("The time in ticks how long the progress bar should be displayed.")
    private long expProgressBarDuration = 120L;
    private DatabaseConfig database = new DatabaseConfig();
    @Comment("Define the expression that calculates the required exp for each level here.")
    private LevelConfig levelConfig = new LevelConfig();
    @Comment("Define what a player automatically gets when he levels up.")
    private LevelUpConfig levelUpConfig = new LevelUpConfig();
    @Comment("Define the expression that calculates the cost for buying new skill slots.")
    private SkillSlotConfig slotConfig = new SkillSlotConfig();
    private SoundConfig sounds = new SoundConfig();

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
    public static class SoundConfig {

        private float volume = 10f;
        private float pitch = 1f;
        private SoundCategory category = SoundCategory.MASTER;
        private String levelUp = Sound.ENTITY_PLAYER_LEVELUP.getKey().toString();
        private String skillUnlock = Sound.BLOCK_BEACON_POWER_SELECT.getKey().toString();
        private String skillActivate = Sound.BLOCK_BEACON_ACTIVATE.getKey().toString();
        private String skillReset = Sound.BLOCK_BEACON_DEACTIVATE.getKey().toString();
        private String slotUnlock = Sound.ENTITY_WITHER_BREAK_BLOCK.getKey().toString();
        private String slotActivate = Sound.BLOCK_ANVIL_USE.getKey().toString();
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

    @ConfigurationElement
    @Getter
    @Setter
    public static class SkillSlotConfig {

        @Comment({
                "You can use any of the following variables and all java Math.* expressions: ",
                "  - x: settable in this config",
                "  - y: settable in this config",
                "  - z: settable in this config",
                "  - a: settable in this config",
                "  - b: settable in this config",
                "  - c: settable in this config",
                "  - level: the current level of the player",
                "  - slots: the number of unlocked slots",
                "  - skills: the number of unlocked skills",
                "  - resets: the number of skill slot resets the player had"
        })
        private String slotPrice = "(Math.pow(2, slots) * x) + (level + skills) * y";
        private String resetPrice = "Math.pow(2, resets) * a";
        @Comment("How many slots can be occupied and the reset remains free?")
        private int freeResets = 0;
        private double x = 1000;
        private double y = 100;
        private double z = 0;
        private double a = 10000;
        private double b = 0;
        private double c = 0;
    }

    @ConfigurationElement
    @Getter
    @Setter
    public static class LevelUpConfig {

        @Comment("The number of skillpoints a player should get each level")
        private int skillPointsPerLevel = 1;
        @Comment("The number of new skill slots a player should get each level")
        private int slotsPerLevel = 0;
        @ElementType(LevelUp.class)
        private Map<Integer, LevelUp> levels = new HashMap<>();

        public Optional<Map.Entry<Integer, LevelUp>> getNextLevelUp(int currentLevel) {

            OptionalInt max = levels.keySet().stream().mapToInt(value -> value).max();
            if (max.isEmpty()) return Optional.empty();

            for (int i = currentLevel + 1; i <= max.getAsInt(); i++) {
                if (levels.containsKey(i)) {
                    return Optional.of(Map.entry(i, levels.get(i)));
                }
            }

            return Optional.empty();
        }

        public Optional<Integer> getNextLevelUpSlot(int currentLevel) {

            OptionalInt max = levels.keySet().stream().mapToInt(value -> value).max();
            if (max.isEmpty()) return Optional.empty();

            for (int i = currentLevel + 1; i <= max.getAsInt(); i++) {
                if (levels.containsKey(i)) {
                    if (levels.get(i).getSlots() > 0) {
                        return Optional.of(i);
                    }
                }
            }

            return Optional.empty();

        }
    }

    @ConfigurationElement
    @Getter
    @Setter
    public static class LevelUp {

        private int skillpoints = 0;
        private int slots = 0;
        private List<String> commands = new ArrayList<>();
    }
}
