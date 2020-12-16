package net.silthus.skills.entities;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import net.silthus.skills.SkillsPlugin;
import net.silthus.skills.requirements.LevelRequirement;
import net.silthus.skills.requirements.PermissionRequirement;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class ConfiguredSkillTest {

    private static final String TEST_SKILL = "test";

    private ServerMock server;
    private SkillsPlugin plugin;

    @BeforeEach
    void setUp() {

        this.server = MockBukkit.mock();
        this.plugin = MockBukkit.load(SkillsPlugin.class);

        MemoryConfiguration cfg = new MemoryConfiguration();
        cfg.set("type", "permission");
        cfg.set("name", "Test Skill");
        cfg.set("level", 5);
        plugin.getSkillManager().loadSkill(TEST_SKILL, cfg);
    }

    @AfterEach
    void tearDown() {

        MockBukkit.unmock();
    }

    @Test
    @DisplayName("should add implicit level requirement based on config")
    void shouldHaveLevelRequirement() {

        assertThat(ConfiguredSkill.findByAliasOrName(TEST_SKILL))
                .isPresent()
                .get()
                .extracting(ConfiguredSkill::requirements)
                .asList()
                .hasAtLeastOneElementOfType(LevelRequirement.class)
                .filteredOn(o -> o instanceof LevelRequirement)
                .extracting("level")
                .contains(5);
    }

    @Test
    @DisplayName("should add implicit permission requirement for alias")
    void shouldHavePermissionRequirementForAlias() {

        assertThat(ConfiguredSkill.findByAliasOrName(TEST_SKILL))
                .isPresent()
                .get()
                .extracting(ConfiguredSkill::requirements)
                .asList()
                .hasAtLeastOneElementOfType(PermissionRequirement.class)
                .filteredOn(o -> o instanceof PermissionRequirement)
                .extracting("permissions")
                .asList()
                .contains(Collections.singletonList(SkillsPlugin.SKILL_PERMISSION_PREFIX + TEST_SKILL));
    }
}