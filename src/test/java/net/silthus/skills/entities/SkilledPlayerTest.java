package net.silthus.skills.entities;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import net.silthus.skills.SkillsPlugin;
import net.silthus.skills.skills.PermissionSkill;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SkilledPlayerTest {

    private ServerMock server;
    private SkillsPlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(SkillsPlugin.class);
    }

    @AfterEach
    void tearDown() {

        MockBukkit.unmock();
    }

    @Test
    void shouldAutoCreatePlayerLevel() {

        SkilledPlayer player = SkilledPlayer.getOrCreate(server.addPlayer());

        assertThat(player.level())
                .isNotNull()
                .extracting(PlayerLevel::level)
                .isEqualTo(1);
    }

    @Test
    void shouldAllowAddingRemovedSkills() {

        SkilledPlayer player = SkilledPlayer.getOrCreate(server.addPlayer());
        ConfiguredSkill skill = new ConfiguredSkill(UUID.randomUUID(), new PermissionSkill(plugin));
        skill.save();

        player.addSkill(skill);
        assertThat(player.hasActiveSkill(skill)).isTrue();

        player.removeSkill(skill);
        assertThat(player.hasActiveSkill(skill)).isFalse();
        assertThat(player.hasSkill(skill)).isFalse();

        player.addSkill(skill);
        assertThat(player.hasActiveSkill(skill)).isTrue();
    }
}