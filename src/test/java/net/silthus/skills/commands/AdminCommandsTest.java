package net.silthus.skills.commands;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import io.ebean.DB;
import net.silthus.skills.SkillManager;
import net.silthus.skills.SkillPluginConfig;
import net.silthus.skills.SkillsPlugin;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import javax.persistence.Table;
import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@Disabled
class AdminCommandsTest {

    private static final String TEST_SKILL = "test";

    private ServerMock server;
    private SkillManager skillManager;
    private AdminCommands commands;

    @BeforeEach
    void setUp(@TempDir Path temp) {

        this.server = MockBukkit.mock();
        this.skillManager = new SkillManager(mock(SkillsPlugin.class), DB.getDefault(), new SkillPluginConfig(new File(temp.toFile(), "config.yml").toPath()));
        skillManager.registerDefaults();
        this.commands = new AdminCommands(skillManager);
    }

    @AfterEach
    void tearDown() {

        MockBukkit.unmock();
    }

    @Nested
    class Commands {

        @BeforeEach
        void setUp() {
            MemoryConfiguration cfg = new MemoryConfiguration();
            cfg.set("type", "permission");
            cfg.set("name", "Test Skill");
            skillManager.loadSkill(TEST_SKILL, cfg);
        }

        @Test
        @DisplayName("add should add skill to player")
        void addShouldAddSkillToPlayer() {

            PlayerMock player = server.addPlayer();
            SkilledPlayer skilledPlayer = skillManager.getPlayer(player);

            commands.addSkill(skilledPlayer, skillManager.getSkill(TEST_SKILL).get(), "bypass");

            assertThat(skilledPlayer.hasSkill(TEST_SKILL));
        }
    }
}