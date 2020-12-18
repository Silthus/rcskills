package de.raidcraft.skills.skills;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.actions.AddSkillAction;
import de.raidcraft.skills.entities.ConfiguredSkill;
import de.raidcraft.skills.entities.SkilledPlayer;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionSkillTest {

    private static final String TEST_SKILL = "test";

    private ServerMock server;
    private SkillsPlugin plugin;

    @BeforeEach
    void setUp() {

        server = MockBukkit.mock();
        plugin = MockBukkit.load(SkillsPlugin.class);
        MemoryConfiguration cfg = new MemoryConfiguration();
        cfg.set("type", "permission");
        cfg.set("name", "Test Skill");
        cfg.set("with.permissions", Collections.singletonList("foobar"));
        plugin.getSkillManager().loadSkill(TEST_SKILL, cfg);
    }

    @AfterEach
    void tearDown() {

        MockBukkit.unmock();
    }

    @Test
    void testSkillShouldExist() {

        assertThat(ConfiguredSkill.findByAliasOrName(TEST_SKILL))
                .isPresent();
    }

    @Test
    @DisplayName("should give player the permission when skill is applied")
    void shouldHavePermissionWhenSkillIsApplied() {

        PlayerMock player = server.addPlayer();
        player.addAttachment(plugin, SkillsPlugin.BYPASS_ACTIVE_SKILL_LIMIT, true);
        player.addAttachment(plugin, SkillsPlugin.SKILL_PERMISSION_PREFIX + TEST_SKILL, true);
        assertThat(player.hasPermission("foobar")).isFalse();

        SkilledPlayer skilledPlayer = SkilledPlayer.getOrCreate(player);
        skilledPlayer.skillSlots(1);
        AddSkillAction.Result result = skilledPlayer
                .addSkill(ConfiguredSkill.findByAliasOrName(TEST_SKILL).get());

        assertThat(result.success()).isTrue();

        result.playerSkill().activate();

        assertThat(player.hasPermission("foobar")).isTrue();
    }

    @Test
    @Disabled // until mockbukkit merges pr #144
    @DisplayName("should remove permission from player when skill is removed")
    void shouldRemovePermissionWhenSkillIsRemoved() {

        PlayerMock player = server.addPlayer();
        assertThat(player.hasPermission("foobar")).isFalse();

        SkilledPlayer skilledPlayer = SkilledPlayer.getOrCreate(player);
        ConfiguredSkill skill = ConfiguredSkill.findByAliasOrName(TEST_SKILL).get();
        skilledPlayer.addSkill(skill);

        assertThat(player.hasPermission("foobar")).isTrue();

        skilledPlayer.removeSkill(skill);
        assertThat(player.hasPermission("foobar")).isFalse();
    }
}