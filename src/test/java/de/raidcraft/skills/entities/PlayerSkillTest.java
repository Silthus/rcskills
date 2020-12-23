package de.raidcraft.skills.entities;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.util.RandomString;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class PlayerSkillTest {

    private static final RandomString rnd = new RandomString();
    private String TEST_SKILL = "test";

    private ServerMock server;
    private SkillsPlugin plugin;

    private SkilledPlayer player;

    private ConfiguredSkill skill;
    @BeforeEach
    void setUp() {

        server = MockBukkit.mock();
        plugin = MockBukkit.load(SkillsPlugin.class);
        TEST_SKILL = rnd.nextString();
        MemoryConfiguration cfg = new MemoryConfiguration();
        cfg.set("type", "permission");
        cfg.set("name", "Test Skill");
        cfg.set("with.permissions", Collections.singletonList("foobar"));
        plugin.getSkillManager().loadSkill(TEST_SKILL, cfg);

        PlayerMock playerMock = server.addPlayer();
        playerMock.setOp(true);
        this.player = SkilledPlayer.getOrCreate(playerMock);
        player.addSkillSlots(5, SkillSlot.Status.FREE);
        skill = ConfiguredSkill.findByAliasOrName(TEST_SKILL).get();
    }

    @AfterEach
    void tearDown() {

        MockBukkit.unmock();
    }

    @Test
    @DisplayName("should activate skill if all requirements are met")
    void shouldActivateSkill() {

        player.addSkill(skill).playerSkill().activate();

        assertThat(PlayerSkill.getOrCreate(player, skill))
                .extracting(PlayerSkill::active)
                .isEqualTo(true);
    }

    @Test
    @DisplayName("should not activate disabled skills")
    void shouldNotActivateSkillThatIsDisabled() {

        skill.enabled(false).save();
        PlayerSkill playerSkill = player.addSkill(skill).playerSkill();
        playerSkill.activate();

        assertThat(playerSkill.active()).isFalse();
    }
}
