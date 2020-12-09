package net.silthus.skills.skills;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.silthus.skills.Skill;
import net.silthus.skills.SkillsPlugin;
import net.silthus.skills.entities.ConfiguredSkill;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.*;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.assertj.core.api.InstanceOfAssertFactories.optional;

class PermissionSkillTest {

    private static final String TEST_SKILL = "test";

    private static ServerMock server;
    private static SkillsPlugin plugin;

    @BeforeAll
    static void setUp() {

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

    @Test
    void testSkillShouldExist() {

        assertThat(ConfiguredSkill.findByAliasOrName(TEST_SKILL))
                .isPresent()
                .get()
                .extracting(ConfiguredSkill::getSkill)
                .asInstanceOf(optional(Skill.class))
                .isPresent()
                .get()
                .extracting("permissions")
                .asInstanceOf(list(String.class))
                .contains("foobar");
    }

    @Test
    @DisplayName("should give player the permission when skill is applied")
    void shouldHavePermissionWhenSkillIsApplied() {

        PlayerMock player = server.addPlayer();
        assertThat(player.hasPermission("foobar")).isFalse();

        SkilledPlayer.getOrCreate(player)
                .addSkill(ConfiguredSkill.findByAliasOrName(TEST_SKILL).get());

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