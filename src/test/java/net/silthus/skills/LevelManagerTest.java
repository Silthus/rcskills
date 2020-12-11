package net.silthus.skills;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import net.silthus.skills.entities.SkilledPlayer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.InvocationTargetException;
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

    private SkillPluginConfig.LevelConfig levelConfig;
    private LevelManager levelManager;
    private SkilledPlayer player;

    @BeforeEach
    void setUp() {

        this.levelConfig = new SkillPluginConfig.LevelConfig();
        this.levelManager = new LevelManager(levelConfig);
        this.player = SkilledPlayer.getOrCreate(server.addPlayer());
    }

    @Test
    @DisplayName("should parse default expression from config")
    void shouldParseDefaultExpression() {

        assertThatCode(() -> levelManager.load())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should parse and evaluate simple expression: level^2")
    void shouldParseSimpleExpression() {

        levelConfig.setExpToNextLevel("Math.pow(level, 2)");
        assertThatCode(() -> levelManager.load()).doesNotThrowAnyException();

        player.level().level(2);
        assertThat(levelManager.calculateExpToNextLevel(player))
                .isEqualTo(4);
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
                Arguments.of(1, 40),
                Arguments.of(2, 160),
                Arguments.of(5, 1000),
                Arguments.of(10, 4000),
                Arguments.of(20, 16000),
                Arguments.of(30, 36000),
                Arguments.of(40, 64000),
                Arguments.of(50, 100000),
                Arguments.of(60, 144000),
                Arguments.of(100, 400000)
        );
    }
}