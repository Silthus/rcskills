package net.silthus.skills.entities;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import net.silthus.skills.SkillsPlugin;
import org.assertj.core.data.TemporalUnitWithinOffset;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

public class PlayerSkillTest {

    private static final String TEST_SKILL = "test";

    private static ServerMock server;
    private static SkillsPlugin plugin;

    @BeforeAll
    static void beforeAll() {

        server = MockBukkit.mock();
        plugin = MockBukkit.load(SkillsPlugin.class);
        MemoryConfiguration cfg = new MemoryConfiguration();
        cfg.set("type", "permission");
        cfg.set("name", "Test Skill");
        cfg.set("with.permissions", Collections.singletonList("foobar"));
        plugin.getSkillManager().loadSkill(TEST_SKILL, cfg);
    }

    @AfterAll
    static void tearDown() {

        MockBukkit.unmock();
    }

    private SkilledPlayer player;
    private ConfiguredSkill skill;

    @BeforeEach
    void setUp() {

        player = SkilledPlayer.getOrCreate(server.addPlayer());
        skill = ConfiguredSkill.findByAliasOrName(TEST_SKILL).get();
    }

    @Test
    @DisplayName("unlock() should set unlock timestamp")
    void shouldUnlockAndActivateSkill() {

        assertThat(player.addSkill(skill).playerSkill().unlocked())
                .isCloseTo(Instant.now(), new TemporalUnitWithinOffset(5, ChronoUnit.SECONDS));
    }
}
