package net.silthus.skills;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import net.silthus.skills.entities.PlayerLevel;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class LevelManagerTest {


    private static ServerMock server;
    private static SkillsPlugin plugin;

    @BeforeAll
    static void beforeAll() {

        server = MockBukkit.mock();
        plugin = MockBukkit.load(SkillsPlugin.class);
    }

    @AfterAll
    static void afterAll() {

        MockBukkit.unmock();
    }

    private LevelManager levelManager;
    private SkilledPlayer player;

    @BeforeEach
    void setUp() {

        this.levelManager = new LevelManager(plugin);
        this.player = SkilledPlayer.getOrCreate(server.addPlayer());
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

        player.level().level(level);
        assertThat(levelManager.calculateExpToNextLevel(player))
                .isEqualTo(result);
    }

    private static Stream levelTestValues() {

        return Stream.of(
                Arguments.of(1, 10),
                Arguments.of(2, 40),
                Arguments.of(5, 250),
                Arguments.of(10, 1000),
                Arguments.of(20, 4000),
                Arguments.of(30, 9000),
                Arguments.of(40, 16000),
                Arguments.of(50, 25000),
                Arguments.of(60, 36000),
                Arguments.of(100, 100000)
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
                Arguments.of(1, 10),
                Arguments.of(2, 50),
                Arguments.of(5, 550),
                Arguments.of(10, 3850),
                Arguments.of(50, 429250),
                Arguments.of(100, 3383500)
        );
    }

    @Test
    @DisplayName("should down level the player when his total exp shrink")
    void shouldDownLevelThePlayerOnExpSet() {

        assertThatCode(() -> levelManager.load()).doesNotThrowAnyException();

        player.level().level(10).save();
        player.level().exp(500).save();

        assertThat(player.level())
                .extracting(PlayerLevel::level, PlayerLevel::totalExp)
                .contains(4, 500L);
    }

    @Test
    @DisplayName("should level up player when exp is added")
    void shouldLevelUpPlayerWhenExpChanges() {

        assertThatCode(() -> levelManager.load()).doesNotThrowAnyException();

        player.level().level(3).save();
        player.level().exp(1400).save();

        assertThat(player.level())
                .extracting(PlayerLevel::level, PlayerLevel::totalExp)
                .contains(7, 1400L);
    }

    @Test
    @DisplayName("should set correct minimum exp when setting higher level")
    void shouldSetCorrectExpWhenSettingLevel() {

        assertThatCode(() -> levelManager.load()).doesNotThrowAnyException();

        player.level().level(10).save();

        assertThat(player.level())
                .extracting(PlayerLevel::level, PlayerLevel::totalExp)
                .contains(10, 3850L);
    }

    @Test
    @DisplayName("should set correct minimum exp when setting lower level")
    void shouldSetCorrectExpWhenSettingLowerLevel() {

        assertThatCode(() -> levelManager.load()).doesNotThrowAnyException();

        player.level().exp(10000);
        player.level().level(10).save();

        assertThat(player.level())
                .extracting(PlayerLevel::level, PlayerLevel::totalExp)
                .contains(10, 3850L);
    }

    @Test
    @DisplayName("should not update exp on level set if within range")
    void shouldNotUpdateExpIfWithinRange() {

        assertThatCode(() -> levelManager.load()).doesNotThrowAnyException();

        player.level().exp(1500);
        player.level().level(7).save();

        assertThat(player.level())
                .extracting(PlayerLevel::level, PlayerLevel::totalExp)
                .contains(7, 1500L);
    }
}