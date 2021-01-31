package de.raidcraft.skills;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import de.raidcraft.skills.entities.Level;
import de.raidcraft.skills.entities.SkilledPlayer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class LevelManagerTest {

    private ServerMock server;
    private RCSkills plugin;
    private LevelManager levelManager;
    private SkilledPlayer player;

    @BeforeEach
    void setUp() {

        server = MockBukkit.mock();
        plugin = MockBukkit.load(RCSkills.class);
        this.levelManager = new LevelManager(plugin);
        this.player = SkilledPlayer.getOrCreate(server.addPlayer());
    }

    @AfterEach
    void afterAll() {

        MockBukkit.unmock();
    }

    @Test
    @DisplayName("should parse default expression from config")
    void shouldParseDefaultExpression() {

        assertThatCode(() -> levelManager.load())
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("levelTestValues")
    void shouldCalculateCorrectDefaultExp(int level, int result) {

        assertThatCode(() -> levelManager.load()).doesNotThrowAnyException();

        player.setLevel(level);
        assertThat(levelManager.calculateExpToNextLevel(player))
                .isEqualTo(result);
    }

    private static Stream levelTestValues() {

        return Stream.of(
                Arguments.of(1, 10),
                Arguments.of(2, 40),
                Arguments.of(5, 250),
                Arguments.of(10, 1000)
        );
    }

    @ParameterizedTest
    @MethodSource("totalExpTestValues")
    void shouldCalculateCorrectTotalExpForLevel(int level, int result) {

        assertThatCode(() -> levelManager.load()).doesNotThrowAnyException();

        assertThat(levelManager.getTotalExpForLevel(level))
                .isEqualTo(result);
    }

    private static Stream totalExpTestValues() {

        return Stream.of(
                Arguments.of(1, 0),
                Arguments.of(2, 10),
                Arguments.of(5, 300),
                Arguments.of(10, 2850),
                Arguments.of(50, 404250),
                Arguments.of(100, 3283500)
        );
    }

    @Test
    @DisplayName("should down level the player when his total exp shrink")
    void shouldDownLevelThePlayerOnExpSet() {

        assertThatCode(() -> levelManager.load()).doesNotThrowAnyException();

        player.setLevel(10).save();
        player.setExp(500).save();

        assertThat(player.level())
                .extracting(Level::getLevel, Level::getTotalExp)
                .contains(5, 500L);
    }

    @Test
    @DisplayName("should level up player when exp is added")
    void shouldLevelUpPlayerWhenExpChanges() {

        assertThatCode(() -> levelManager.load()).doesNotThrowAnyException();

        player.setLevel(3).save();
        player.setExp(1400).save();

        assertThat(player.level())
                .extracting(Level::getLevel, Level::getTotalExp)
                .contains(8, 1400L);
    }

    @Test
    @DisplayName("should set correct minimum exp when setting higher level")
    void shouldSetCorrectExpWhenSettingLevel() {

        assertThatCode(() -> levelManager.load()).doesNotThrowAnyException();

        player.setLevel(10).save();

        assertThat(player.level())
                .extracting(Level::getLevel, Level::getTotalExp)
                .contains(10, 2850L);
    }

    @Test
    @DisplayName("should set correct minimum exp when setting lower level")
    void shouldSetCorrectExpWhenSettingLowerLevel() {

        assertThatCode(() -> levelManager.load()).doesNotThrowAnyException();

        player.setExp(10000);
        player.setLevel(10).save();

        assertThat(player.level())
                .extracting(Level::getLevel, Level::getTotalExp)
                .contains(10, 2850L);
    }

    @Test
    @DisplayName("should not update exp on level set if within range")
    void shouldNotUpdateExpIfWithinRange() {

        assertThatCode(() -> levelManager.load()).doesNotThrowAnyException();

        player.setExp(1500);
        player.setLevel(7).save();

        assertThat(player.level())
                .extracting(Level::getLevel, Level::getTotalExp)
                .contains(7, 910L);
    }

    @Test
    @DisplayName("should award player rewards for leveling")
    void shouldAwardPlayerRewardsForLeveling() {

        assertThatCode(() -> levelManager.load()).doesNotThrowAnyException();
        SkillPluginConfig.LevelUp levelUp = new SkillPluginConfig.LevelUp();
        levelUp.setSlots(1);
        plugin.getPluginConfig().getLevelUpConfig().getLevels().put(5, levelUp);
        plugin.getPluginConfig().getLevelUpConfig().getLevels().put(10, levelUp);
        plugin.getPluginConfig().getLevelUpConfig().getLevels().put(15, levelUp);
        plugin.getPluginConfig().getLevelUpConfig().getLevels().put(16, levelUp);

        player.setLevel(3).save();
        assertThat(player.skillSlots().size()).isEqualTo(0);

        player.addLevel(12).save();

        assertThat(player.skillSlots().size()).isEqualTo(3);
    }
}