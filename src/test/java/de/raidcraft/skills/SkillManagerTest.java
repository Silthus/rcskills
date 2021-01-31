package de.raidcraft.skills;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import de.raidcraft.skills.entities.ConfiguredSkill;
import de.raidcraft.skills.entities.SkilledPlayer;
import lombok.NonNull;
import net.silthus.ebean.BaseEntity;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class SkillManagerTest {

    private ServerMock server;
    private RCSkills plugin;
    private SkillManager skillManager;

    @BeforeEach
    void setUp(@TempDir Path temp) {

        this.server = MockBukkit.mock();
        this.plugin = MockBukkit.load(RCSkills.class);
        this.skillManager = new SkillManager(plugin, new SkillPluginConfig(new File(temp.toFile(), "config.yml").toPath()));
    }

    @AfterEach
    void tearDown() {

        MockBukkit.unmock();
    }

    @Nested
    @DisplayName("registerRequirement(...)")
    class registerRequirement {

        @Test
        @DisplayName("should register custom requirement types")
        void shouldAllowRegistrationOfCustomRequirements() {

            skillManager.registerRequirement(CustomRequirement.class, CustomRequirement::new);

            Assertions.assertThat(skillManager.requirements()).extractingByKey("test")
                    .isNotNull()
                    .extracting(Requirement.Registration::requirementClass)
                    .isEqualTo(CustomRequirement.class);
        }

        @Test
        @DisplayName("should not allow registration of duplicate types")
        void shouldNotRegisterDuplicateTypeIdentifiers() {

            skillManager.registerRequirement(CustomRequirement.class, CustomRequirement::new);

            assertThatCode(() -> skillManager.registerRequirement(SecondRequirement.class, SecondRequirement::new))
                    .doesNotThrowAnyException();

            Assertions.assertThat(skillManager.requirements()).extractingByKey("test")
                    .isNotNull()
                    .extracting(Requirement.Registration::requirementClass)
                    .isEqualTo(CustomRequirement.class);
        }
    }

    @Nested
    @DisplayName("loadRequirements(...)")
    class loadRequirements {

        @BeforeEach
        void setUp() {
            skillManager.registerRequirement(CustomRequirement.class, CustomRequirement::new);
            skillManager.registerRequirement(ThirdRequirement.class, ThirdRequirement::new);
        }

        @Test
        @DisplayName("should load all requirements from the config and use the key as identifier")
        void shouldLoadRequirementsFromConfig() {

            MemoryConfiguration config = new MemoryConfiguration();
            MemoryConfiguration foo = new MemoryConfiguration();
            foo.set("type", "test");
            config.set("foo", foo);
            MemoryConfiguration bar = new MemoryConfiguration();
            bar.set("type", "test3");
            config.set("bar", bar);

            Assertions.assertThat(skillManager.loadRequirements(config))
                    .hasSize(2)
                    .extracting(Requirement::type)
                    .contains("test", "test3");
        }

        @Test
        @DisplayName("should return empty array if config is null")
        void shouldAllowNullConfigSection() {

            Assertions.assertThat(skillManager.loadRequirements(null)).isEmpty();
        }

        @Test
        @DisplayName("should throw on invalid requirement types")
        void shouldNotLoadInvalidTypes() {

            MemoryConfiguration config = new MemoryConfiguration();
            MemoryConfiguration foo = new MemoryConfiguration();
            foo.set("type", "foobar");
            config.set("foo", foo);
            MemoryConfiguration bar = new MemoryConfiguration();
            bar.set("type", "test3");
            config.set("bar", bar);

            Assertions.assertThat(skillManager.loadRequirements(config))
                    .hasSize(1)
                    .hasOnlyElementsOfType(ThirdRequirement.class);
        }
    }

    @Nested
    @DisplayName("loadSkills(...)")
    class loadSkills {

        public Path skillsPath;

        @BeforeEach
        void setUp(@TempDir Path temp) throws IOException {

            Path path = Paths.get("src", "test", "resources", "skills");
            FileUtils.copyDirectory(path.toFile(), temp.toFile());
            skillsPath = temp;
            skillManager.registerDefaults();
        }

        @Test
        @DisplayName("should load all skills from a path")
        void shouldLoadSkillsFromPath() {

            Assertions.assertThat(skillManager.loadSkills(skillsPath))
                    .hasSizeGreaterThan(2)
                    .extracting(ConfiguredSkill::alias)
                    .contains("test", "foobar", "nested.minimal");
        }

        @Test
        @DisplayName("should disable old skills")
        void shouldDisableOldSkills() {

            ConfiguredSkill skill = ConfiguredSkill.getOrCreate(UUID.randomUUID());
            skill.save();

            skillManager.load();

            assertThat(ConfiguredSkill.find.byId(skill.id()))
                    .extracting(ConfiguredSkill::enabled)
                    .isEqualTo(false);
        }
    }


    @Nested
    @DisplayName("getPlayer(...)")
    class getPlayer {

        @Test
        @DisplayName("should always return a player regardless if it exists in the db")
        void shouldAlwaysReturnAPlayer() {

            PlayerMock player = server.addPlayer();
            Assertions.assertThat(SkilledPlayer.getOrCreate(player))
                    .isNotNull()
                    .extracting(BaseEntity::id, SkilledPlayer::name)
                    .contains(player.getUniqueId(), player.getName());
        }
    }

    @RequirementInfo("test")
    static class CustomRequirement extends AbstractRequirement {

        @Override
        public TestResult test(@NonNull SkilledPlayer target) {

            return TestResult.ofSuccess();
        }

        @Override
        protected void loadConfig(ConfigurationSection config) {

        }
    }

    @RequirementInfo("test")
    static class SecondRequirement extends AbstractRequirement {

        @Override
        public TestResult test(@NonNull SkilledPlayer target) {
            return TestResult.ofSuccess();
        }

        @Override
        protected void loadConfig(ConfigurationSection config) {

        }
    }

    @RequirementInfo("test3")
    static class ThirdRequirement extends AbstractRequirement {
        @Override
        public TestResult test(@NonNull SkilledPlayer target) {
            return TestResult.ofSuccess();
        }

        @Override
        protected void loadConfig(ConfigurationSection config) {

        }
    }

}