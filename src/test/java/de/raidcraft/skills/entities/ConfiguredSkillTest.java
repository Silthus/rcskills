package de.raidcraft.skills.entities;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import de.raidcraft.skills.ExecutionConfig;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.TaskConfig;
import de.raidcraft.skills.requirements.LevelRequirement;
import de.raidcraft.skills.requirements.PermissionRequirement;
import de.raidcraft.skills.requirements.SkillRequirement;
import org.assertj.core.groups.Tuple;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ALL")
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
        cfg.set("restricted", true);
        cfg.set("level", 5);
        cfg.set("with.permissions", Arrays.asList("foobar", "foo"));
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
    @DisplayName("should add implicit permission requirement for alias if restricted")
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

    @Test
    @DisplayName("should only serialize config pure values and not nested top level keys")
    void shouldSerializeConfigWithoutConfigSectionToStrings() {

        ConfiguredSkill skill = ConfiguredSkill.findByAliasOrName(TEST_SKILL).get();
        assertThat(skill.config())
                .doesNotContainKey("with")
                .containsKey("with.permissions");
    }

    @Nested
    @DisplayName("Parent -> Child")
    class ParentChildSkills {

        private static final String parent = "parent";
        private static final String child1 = "parent:child1";
        private static final String child2 = "parent:child1:child2";

        private ConfiguredSkill loadSkill() {

            return loadSkill(parent, configurationSection -> {
            });
        }

        private ConfiguredSkill loadSkill(String alias) {

            return loadSkill(alias, configurationSection -> {
            });
        }

        private ConfiguredSkill loadSkill(String alias, Consumer<ConfigurationSection> config) {

            MemoryConfiguration cfg = new MemoryConfiguration();
            cfg.set("type", "none");
            cfg.set("level", 5);
            cfg.set("skills.child1.name", child1);
            cfg.set("skills.child1.skills.child2.name", child2);
            config.accept(cfg);

            plugin.getSkillManager().loadSkill(parent, cfg);

            return getOrAssertSkill(alias);
        }

        private ConfiguredSkill getOrAssertSkill(String name) {

            Optional<ConfiguredSkill> skill = ConfiguredSkill.findByAliasOrName(name);
            assertThat(skill).isPresent();
            return skill.get();
        }

        @Test
        @DisplayName("should load child skills recursively")
        void shouldLoadChildSkillsRecursively() {

            ConfiguredSkill skill = loadSkill();
            assertThat(skill)
                    .extracting(ConfiguredSkill::isParent, s -> s.children().size())
                    .contains(true, 1);

            assertThat(skill.children())
                    .hasSize(1)
                    .extracting(
                            ConfiguredSkill::name,
                            ConfiguredSkill::isParent,
                            ConfiguredSkill::isChild,
                            s -> s.children().size(),
                            ConfiguredSkill::level
                    )
                    .contains(Tuple.tuple(
                            child1,
                            true,
                            true,
                            1,
                            5
                    ));

            assertThat(getOrAssertSkill(child2))
                    .extracting(ConfiguredSkill::type, ConfiguredSkill::isParent, ConfiguredSkill::isChild)
                    .contains("none", false, true);
        }

        @Test
        @DisplayName("should hide sub skills by default")
        void shouldHideSubSkillsByDefault() {

            assertThat(loadSkill(child1))
                    .extracting(ConfiguredSkill::hidden)
                    .isEqualTo(true);
        }

        @Test
        @DisplayName("should allow overriding hidden status")
        void shouldAllowOverridingHiddenStatus() {

            assertThat(loadSkill(child1, cfg -> {
                cfg.set("skills.child1.hidden", false);
            })).extracting(ConfiguredSkill::hidden)
                    .isEqualTo(false);
        }

        @Test
        @DisplayName("should add skill requirement to child skills")
        void shouldAddSkillRequirementtoChildren() {

            assertThat(loadSkill(child1))
                    .extracting(ConfiguredSkill::requirements)
                    .asList()
                    .hasAtLeastOneElementOfType(SkillRequirement.class)
                    .filteredOn(o -> o instanceof SkillRequirement)
                    .extracting("skill", "hidden")
                    .contains(Tuple.tuple(ConfiguredSkill.findByAliasOrName(parent).get(), true));
        }

        @Test
        @DisplayName("should not restrict child skills and require permission")
        void shouldNotRequirePermissionForRestrictedChilds() {

            assertThat(loadSkill(child1, cfg -> cfg.set("restricted", true)))
                    .extracting(ConfiguredSkill::restricted)
                    .isEqualTo(false);
        }

        @Test
        @DisplayName("should disabled parent skill if disable-parent is set")
        void shouldDisableParentSkillIfSet() {

            assertThat(loadSkill(child1, cfg -> cfg.set("skills.child1.disable-parent", true)))
                    .extracting(ConfiguredSkill::replacedSkills)
                    .asList()
                    .contains(getOrAssertSkill(parent));
        }

        @Test
        @DisplayName("should copy parent \"with\" section into child skills")
        void shouldCopyParentSkillConfigIntoChilds() {

            ConfigurationSection config = loadSkill(child1, cfg -> cfg.set("with.exp", 25)).getSkillConfig();

            assertThat(config.getInt("exp")).isEqualTo(25);
        }

        @Test
        @DisplayName("should copy execution config section into child skills")
        void shouldCopyExecutionConfigIntoChildSkills() {

            ExecutionConfig executionConfig = loadSkill(child2, cfg -> cfg.set("execution.cooldown", "10s")).executionConfig();

            assertThat(executionConfig.cooldown()).isEqualTo(10000L);
        }

        @Test
        @DisplayName("should copy task config into child skill")
        void shouldCopyTaskConfigIntoChildSkills() {

            TaskConfig taskConfig = loadSkill(child2, cfg -> cfg.set("task.interval", 100)).taskConfig();

            assertThat(taskConfig.interval()).isEqualTo(100L);
        }

        @Test
        @DisplayName("should allow overrding parent skill section config values")
        void shouldAllowOverridingOfParentProperties() {

            ConfiguredSkill skill = loadSkill(child1, cfg -> {
                cfg.set("with.exp", 25);
                cfg.set("with.interval", 100);
                cfg.set("skills.child1.with.exp", 50);
            });

            ConfigurationSection skillConfig = skill.getSkillConfig();
            assertThat(skillConfig.getInt("exp")).isEqualTo(50);
            assertThat(skillConfig.getInt("interval")).isEqualTo(100);
        }

        @Test
        @DisplayName("should set enabled worlds property")
        void shouldSetEnabledWorldsArray() {

            ConfiguredSkill skill = loadSkill(parent, cfg -> cfg.set("worlds", Arrays.asList("world")));

            assertThat(skill.worlds()).containsOnly("world");
        }

        @Test
        @DisplayName("should set disabled worlds property")
        void shouldSetDisabledWorldsArray() {

            ConfiguredSkill skill = loadSkill(parent, cfg -> cfg.set("disabled-worlds", Arrays.asList("world")));

            assertThat(skill.disabledWorlds()).containsOnly("world");
        }

        @Test
        @DisplayName("should propagate enabled worlds to child skills")
        void shouldPropagateEnabledWorldsToChildren() {

            ConfiguredSkill skill = loadSkill(child1, cfg -> cfg.set("worlds", Arrays.asList("world")));

            assertThat(skill.disabledWorlds()).containsOnly("world");
        }

        @Test
        @DisplayName("should propagate disable worlds to child skills")
        void shouldPropagateDisabledWorldsToChildren() {

            ConfiguredSkill skill = loadSkill(child2, cfg -> cfg.set("disabled-worlds", Arrays.asList("world")));

            assertThat(skill.disabledWorlds()).containsOnly("world");
        }
    }
}