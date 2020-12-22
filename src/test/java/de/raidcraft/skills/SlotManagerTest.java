package de.raidcraft.skills;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import de.raidcraft.skills.entities.SkilledPlayer;
import org.assertj.core.util.Strings;
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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class SlotManagerTest {

    private ServerMock server;
    private SkillsPlugin plugin;
    private SkillPluginConfig.SkillSlotConfig config;
    private SlotManager slotManager;
    private SkilledPlayer player;

    @BeforeEach
    void setUp() {

        server = MockBukkit.mock();
        plugin = MockBukkit.load(SkillsPlugin.class);
        config = plugin.getPluginConfig().getSlotConfig();
        this.slotManager = new SlotManager();
        this.player = spy(SkilledPlayer.getOrCreate(server.addPlayer()));
    }

    @AfterEach
    void afterAll() {

        MockBukkit.unmock();
    }

    @Test
    @DisplayName("should parse default slot expression correctly")
    void shouldParseDefaultSlotExpression() {

        assertThatCode(() -> slotManager.load(config))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @DisplayName("should calculate correct slot cost")
    @MethodSource("slotTestValues")
    void shouldCalculateCorrectSlotCost(String formula, int level, int skills, int slots, double result) {

        if (!Strings.isNullOrEmpty(formula)) {
            config.setSlotPrice(formula);
        }

        assertThatCode(() -> slotManager.load(config)).doesNotThrowAnyException();

        when(player.slotCount()).thenReturn(slots);
        when(player.skillCount()).thenReturn(skills);
        player.setLevel(level);

        assertThat(slotManager.calculateSlotCost(player))
                .isEqualTo(result);
    }

    private static Stream slotTestValues() {

        return Stream.of(
                Arguments.of("", 1, 10, 1, 3100),
                Arguments.of("level * slots", 10, 1, 10, 100)
        );
    }

    @ParameterizedTest
    @DisplayName("should calculate correct slot cost")
    @MethodSource("slotResetTestValues")
    void shouldCalculateCorrectSlotResetCost(String formula, int level, int skills, int slots, int resets, double result) {

        if (!Strings.isNullOrEmpty(formula)) {
            config.setResetPrice(formula);
        }

        assertThatCode(() -> slotManager.load(config)).doesNotThrowAnyException();

        when(player.slotCount()).thenReturn(slots);
        when(player.skillCount()).thenReturn(skills);
        when(player.resetCount()).thenReturn(resets);
        player.setLevel(level);

        assertThat(slotManager.calculateSlotResetCost(player))
                .isEqualTo(result);
    }

    private static Stream slotResetTestValues() {

        return Stream.of(
                Arguments.of("", 1, 10, 1, 1, 20000),
                Arguments.of("resets * 100", 10, 1, 10, 3, 300)
        );
    }

}