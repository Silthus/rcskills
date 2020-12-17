package net.silthus.skills;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Disabled
public class IntegrationTest {

    private static final String TEST_SKILL = "test";

    private ServerMock server;
    private SkillsPlugin plugin;

    @BeforeEach
    void setUp() {

        this.server = MockBukkit.mock(new net.silthus.skills.ServerMock());
        this.plugin = MockBukkit.load(SkillsPlugin.class);
        plugin.setupCommands();

        MemoryConfiguration cfg = new MemoryConfiguration();
        cfg.set("type", "permission");
        cfg.set("name", "Test Skill");
        plugin.getSkillManager().loadSkill(TEST_SKILL, cfg);
    }

    @AfterEach
    void tearDown() {

        MockBukkit.unmock();
    }

    @Nested
    @DisplayName("Commands")
    class Commands {

        private Player player;

        @BeforeEach
        void setUp() {
            player = server.addPlayer();
            player.setOp(true);
        }

        @Nested
        @DisplayName("/rcs:admin")
        class AdminCommands {

            @Nested
            @DisplayName("add <player> <skill>")
            class add {

                @Test
                @DisplayName("should add the given skill to the player")
                void shouldAddSkillToPlayer() {

                    server.dispatchCommand(server.getConsoleSender(),"rcs:admin add skill " + player.getName() + " " + TEST_SKILL);
                    assertThat(SkilledPlayer.getOrCreate(player).hasSkill(TEST_SKILL)).isTrue();
                }
            }
        }

        @Nested
        @DisplayName("/rcs")
        class PlayerCommands {

            @Nested
            @DisplayName("buy <skill> [player]")
            class buy {

                @Test
                @DisplayName("player should be able to buy skills when all requirements are met")
                void shouldBuySkillIfAllRequirementsAreMet() {

                    server.dispatchCommand(player, "rcs buy " + TEST_SKILL);
                    assertThat(SkilledPlayer.getOrCreate(player).hasActiveSkill(TEST_SKILL)).isTrue();
                }
            }
        }
    }
}
