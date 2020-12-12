package net.silthus.skills;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import lombok.NonNull;
import net.silthus.ebean.BaseEntity;
import net.silthus.skills.entities.ConfiguredSkill;
import net.silthus.skills.entities.SkilledPlayer;
import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

class SkillManagerTest {

    private ServerMock server;
    private SkillManager skillManager;

    @BeforeEach
    void setUp(@TempDir Path temp) {

        this.server = MockBukkit.mock();
        this.skillManager = new SkillManager(mock(SkillsPlugin.class), new SkillPluginConfig(new File(temp.toFile(), "config.yml").toPath()));
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

            assertThat(skillManager.requirements()).extractingByKey("test")
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

            assertThat(skillManager.requirements()).extractingByKey("test")
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

            assertThat(skillManager.loadRequirements(config))
                    .hasSize(2)
                    .extracting(Requirement::type)
                    .contains("test", "test3");
        }

        @Test
        @DisplayName("should return empty array if config is null")
        void shouldAllowNullConfigSection() {

            assertThat(skillManager.loadRequirements(null)).isEmpty();
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

            assertThat(skillManager.loadRequirements(config))
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

            assertThat(skillManager.loadSkills(skillsPath))
                    .hasSizeGreaterThan(2)
                    .extracting(ConfiguredSkill::alias)
                    .contains("test", "foobar", "nested.minimal");
        }
    }

    @Nested
    @DisplayName("loadPlayerSkills(...)")
    class loadPlayerSkills {

        @BeforeEach
        void setUp() {

            skillManager.registerDefaults();
        }

        @Test
        @DisplayName("should load and apply all player skills")
        void shouldLoadAllPlayerSkillsFromTheDatabase() {


        }
    }


    @Nested
    @DisplayName("getPlayer(...)")
    class getPlayer {

        @Test
        @DisplayName("should always return a player regardless if it exists in the db")
        void shouldAlwaysReturnAPlayer() {

            PlayerMock player = server.addPlayer();
            assertThat(SkilledPlayer.getOrCreate(player))
                    .isNotNull()
                    .extracting(BaseEntity::id, SkilledPlayer::name)
                    .contains(player.getUniqueId(), player.getName());
        }
    }

    @RequirementType("test")
    static class CustomRequirement extends AbstractRequirement {

        @Override
        public TestResult test(@NonNull SkilledPlayer target) {

            return TestResult.ofSuccess();
        }

        @Override
        protected void loadConfig(ConfigurationSection config) {

        }
    }

    @RequirementType("test")
    static class SecondRequirement extends AbstractRequirement {

        @Override
        public TestResult test(@NonNull SkilledPlayer target) {
            return TestResult.ofSuccess();
        }

        @Override
        protected void loadConfig(ConfigurationSection config) {

        }
    }

    @RequirementType("test3")
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