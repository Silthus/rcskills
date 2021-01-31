package de.raidcraft.skills.entities;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import de.raidcraft.skills.RCSkills;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("ALL")
class SkilledPlayerTest {

    private ServerMock server;
    private RCSkills plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(RCSkills.class);
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

    @Test
    @DisplayName("should auto unlock news skills")
    void shouldAutoUnlockNewSkills() {

        ConfiguredSkill skill = new ConfiguredSkill(UUID.randomUUID())
                .autoUnlock(true)
                .skillpoints(0)
                .noSkillSlot(true)
                .level(5);
        skill.save();

        PlayerMock bukkitPlayer = server.addPlayer();
        bukkitPlayer.setOp(true);

        SkilledPlayer skilledPlayer = SkilledPlayer.getOrCreate(bukkitPlayer);
        skilledPlayer.setLevel(4);
        assertThat(SkilledPlayer.getOrCreate(bukkitPlayer).hasSkill(skill))
                .isFalse();

        skilledPlayer.setLevel(5);

        assertThat(SkilledPlayer.getOrCreate(bukkitPlayer).hasActiveSkill(skill))
                .isTrue();
    }

    @Test
    @DisplayName("delete should delete player and all related entries from the database")
    void shouldDeletePlayer() {

        ConfiguredSkill skill = new ConfiguredSkill(UUID.randomUUID())
                .autoUnlock(true)
                .skillpoints(0)
                .level(5);
        skill.save();

        PlayerMock bukkitPlayer = server.addPlayer();
        bukkitPlayer.setOp(true);

        SkilledPlayer skilledPlayer = SkilledPlayer.getOrCreate(bukkitPlayer);
        skilledPlayer.setLevel(4);
        skilledPlayer.setSkillSlots(1, SkillSlot.Status.FREE);
        assertThat(SkilledPlayer.getOrCreate(bukkitPlayer).hasSkill(skill))
                .isFalse();

        skilledPlayer.setLevel(5);
        PlayerSkill.getOrCreate(skilledPlayer, skill).activate();
        assertThat(SkilledPlayer.getOrCreate(bukkitPlayer).hasActiveSkill(skill))
                .isTrue();

        assertThat(skilledPlayer.delete()).isTrue();
        assertThat(SkilledPlayer.find.byId(bukkitPlayer.getUniqueId())).isNull();
    }

    @Test
    @DisplayName("should set and store the given skill binding")
    void shouldSetTheSkillBinding() {

        UUID skillId = UUID.randomUUID();
        SkilledPlayer player = SkilledPlayer.getOrCreate(server.addPlayer());
        PlayerSkill playerSkill = mock(PlayerSkill.class);
        when(playerSkill.id()).thenReturn(skillId);
        when(playerSkill.player()).thenReturn(player);

        ItemBindings bindings = player.bindings();
        bindings.bind(
                playerSkill,
                Material.STICK,
                ItemBinding.Action.RIGHT_CLICK
        );

        assertThat(ItemBinding.find(player.id()))
                .asList()
                .hasSize(1);
        assertThat(bindings.contains(Material.STICK, ItemBinding.Action.RIGHT_CLICK)).isTrue();
    }

    @Test
    @DisplayName("should remove the player binding for the given material")
    void shouldRemoveTheBinding() {

        SkilledPlayer player = SkilledPlayer.getOrCreate(server.addPlayer());
        PlayerSkill playerSkill = mock(PlayerSkill.class);
        when(playerSkill.id()).thenReturn(UUID.randomUUID());
        when(playerSkill.player()).thenReturn(player);

        ItemBindings bindings = player.bindings();
        bindings.bind(
                playerSkill,
                Material.STICK,
                ItemBinding.Action.RIGHT_CLICK
        );

        bindings.unbind(Material.STICK);

        assertThat(ItemBinding.find(player.id()))
                .asList()
                .isEmpty();
        assertThat(bindings.contains(Material.STICK)).isFalse();
    }
}