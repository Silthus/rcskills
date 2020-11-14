package net.silthus.skills;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import io.ebean.DB;
import lombok.NonNull;
import net.silthus.ebean.BaseEntity;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
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
        this.skillManager = new SkillManager(mock(SkillsPlugin.class), DB.getDefault(), new SkillPluginConfig(new File(temp.toFile(), "config.yml").toPath()));
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

        private Path testResources = Paths.get("src","test","resources", "skills");

        @BeforeEach
        void setUp() {

            skillManager.registerDefaults();
        }

        @Test
        @DisplayName("should load all skills from a path")
        void shouldLoadSkillsFromPath() {

            assertThat(skillManager.loadSkills(testResources))
                    .hasSizeGreaterThan(2)
                    .extracting(ConfiguredSkill::identifier)
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
            assertThat(skillManager.getPlayer(player))
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
    }

    @RequirementType("test")
    static class SecondRequirement extends AbstractRequirement {

        @Override
        public TestResult test(@NonNull SkilledPlayer target) {
            return TestResult.ofSuccess();
        }
    }

    @RequirementType("test3")
    static class ThirdRequirement extends AbstractRequirement {
        @Override
        public TestResult test(@NonNull SkilledPlayer target) {
            return TestResult.ofSuccess();
        }
    }

}