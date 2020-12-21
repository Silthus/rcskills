package de.raidcraft.skills.entities;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import de.raidcraft.skills.SkillsPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
                .extracting(Level::getLevel)
                .isEqualTo(1);
    }

    @Test
    void shouldAllowAddingRemovedSkills() {

        PlayerMock serverPlayer = server.addPlayer();
        serverPlayer.setOp(true);
        SkilledPlayer player = SkilledPlayer.getOrCreate(serverPlayer);
        ConfiguredSkill skill = new ConfiguredSkill(UUID.randomUUID());
        skill.save();

        player.addSkill(skill);
        assertThat(player.hasSkill(skill)).isTrue();

        player.removeSkill(skill);
        assertThat(player.hasSkill(skill)).isFalse();

        player.addSkill(skill);
        assertThat(player.hasSkill(skill)).isTrue();
    }

    @Test
    @DisplayName("should sum up skillslots from active skills")
    void shouldSumReturnCorrectFreeSkillSlots() {

        ConfiguredSkill skill1 = new ConfiguredSkill(UUID.randomUUID());
        skill1.save();
        ConfiguredSkill skill2 = new ConfiguredSkill(UUID.randomUUID());
        skill2.save();
        ConfiguredSkill skill3 = new ConfiguredSkill(UUID.randomUUID());
        skill3.noSkillSlot(true);
        skill3.save();

        PlayerMock bukkitPlayer = server.addPlayer();
        bukkitPlayer.setOp(true);

        SkilledPlayer player = SkilledPlayer.getOrCreate(bukkitPlayer);
        player.setSkillSlots(10, SkillSlot.Status.FREE);
        player.addSkill(skill1, true).playerSkill().activate();
        player.addSkill(skill2, true).playerSkill().activate();
        player.addSkill(skill3, true).playerSkill().activate();
        player.save();

        assertThat(SkilledPlayer.getOrCreate(bukkitPlayer).freeSkillSlots()).isEqualTo(8);
    }
}