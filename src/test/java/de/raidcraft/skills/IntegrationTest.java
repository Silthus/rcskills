package de.raidcraft.skills;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import de.raidcraft.skills.entities.PlayerSkill;
import de.raidcraft.skills.entities.SkilledPlayer;
import de.raidcraft.skills.util.RandomString;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ALL")
public class IntegrationTest {

    private static final String TEST_SKILL = "test";
    private static final RandomString random = new RandomString();

    private ServerMock server;
    private SkillsPlugin plugin;

    @BeforeEach
    void setUp() {

        this.server = MockBukkit.mock(new de.raidcraft.skills.ServerMock());
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

    private void assertSkillIsActive(Player player, String skill) {

        assertThat(SkilledPlayer.getOrCreate(player).getSkill(TEST_SKILL))
                .isPresent()
                .get()
                .extracting(PlayerSkill::status, PlayerSkill::active, PlayerSkill::unlocked)
                .contains(SkillStatus.ACTIVE, true, true);
    }

    private void assertSkillIsUnlocked(Player player, String skill) {

        assertThat(SkilledPlayer.getOrCreate(player).getSkill(TEST_SKILL))
                .isPresent()
                .get()
                .extracting(PlayerSkill::status, PlayerSkill::active, PlayerSkill::unlocked)
                .contains(SkillStatus.UNLOCKED, false, true);

    }

    @Nested
    @DisplayName("Commands")
    class Commands {

        private Player player;

        @BeforeEach
        void setUp() {
            player = server.addPlayer(random.nextString());
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

                    server.dispatchCommand(player,"rcsa add skill " + player.getName() + " " + TEST_SKILL);
                    assertSkillIsUnlocked(player, TEST_SKILL);
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
                    server.dispatchCommand(player, "rcs buyconfirm");
                    assertSkillIsUnlocked(player, TEST_SKILL);
                }
            }
        }
    }
}
