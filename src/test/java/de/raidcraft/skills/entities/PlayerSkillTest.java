package de.raidcraft.skills.entities;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import de.raidcraft.skills.SkillsPlugin;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class PlayerSkillTest {

    private static final String TEST_SKILL = "test";

    private ServerMock server;
    private SkillsPlugin plugin;

    private SkilledPlayer player;

    private ConfiguredSkill skill;
    @BeforeEach
    void setUp() {

        server = MockBukkit.mock();
        plugin = MockBukkit.load(SkillsPlugin.class);
        MemoryConfiguration cfg = new MemoryConfiguration();
        cfg.set("type", "permission");
        cfg.set("name", "Test Skill");
        cfg.set("slots", 0);
        cfg.set("with.permissions", Collections.singletonList("foobar"));
        plugin.getSkillManager().loadSkill(TEST_SKILL, cfg);

        PlayerMock playerMock = server.addPlayer();
        playerMock.setOp(true);
        this.player = SkilledPlayer.getOrCreate(playerMock);
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
}
