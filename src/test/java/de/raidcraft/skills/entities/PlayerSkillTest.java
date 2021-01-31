package de.raidcraft.skills.entities;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import de.raidcraft.skills.DefaultSkillContextTest;
import de.raidcraft.skills.ExecutionResult;
import de.raidcraft.skills.RCSkills;
import de.raidcraft.skills.actions.AddSkillAction;
import de.raidcraft.skills.util.RandomString;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("ALL")
public class PlayerSkillTest {

    private static final RandomString rnd = new RandomString();
    private String TEST_SKILL = "test";

    private ServerMock server;
    private RCSkills plugin;

    private SkilledPlayer player;
    private PlayerMock playerMock;

    private ConfiguredSkill skill;

    @BeforeEach
    void setUp() {

        server = MockBukkit.mock();
        plugin = MockBukkit.load(RCSkills.class);

        TEST_SKILL = rnd.nextString();
        MemoryConfiguration cfg = new MemoryConfiguration();
        cfg.set("type", "permission");
        cfg.set("name", "Test Skill");
        cfg.set("auto-activate", false);
        cfg.set("with.permissions", Collections.singletonList("foobar"));
        plugin.getSkillManager().loadSkill(TEST_SKILL, cfg);

        playerMock = server.addPlayer(rnd.nextString());
        this.player = SkilledPlayer.getOrCreate(playerMock);
        player.addSkillSlots(20, SkillSlot.Status.FREE);
        skill = ConfiguredSkill.findByAliasOrName(TEST_SKILL).get();
    }

    @AfterEach
    void tearDown() {

        SkilledPlayer.find.all().forEach(SkilledPlayer::delete);
        ConfiguredSkill.find.all().stream()
                .filter(skill1 -> !skill1.isChild())
                .forEach(ConfiguredSkill::delete);

        MockBukkit.unmock();
    }

    @Test
    @DisplayName("should activate skill if all requirements are met")
    void shouldActivateSkill() {

        player.addSkill(skill).playerSkill().activate();

        assertThat(PlayerSkill.getOrCreate(player, skill))
                .extracting(PlayerSkill::active)
                .isEqualTo(true);
        assertThat(playerMock.hasPermission("foobar")).isTrue();
    }

    @Test
    @DisplayName("should not activate disabled skills")
    void shouldNotActivateSkillThatIsDisabled() {

        skill.enabled(false).save();
        PlayerSkill playerSkill = player.addSkill(skill).playerSkill();
        playerSkill.activate();

        assertThat(playerSkill.active()).isFalse();
        assertThat(playerMock.hasPermission("foobar")).isFalse();
    }

    @Test
    @DisplayName("should disable skill if it was active")
    void shouldDisableSkillIfItIsActive() {

        PlayerSkill skill = player.addSkill(this.skill).playerSkill();

        skill.activate();
        assertThat(playerMock.hasPermission("foobar")).isTrue();

        skill.disable();
        assertThat(playerMock.hasPermission("foobar")).isFalse();
    }

    @Test
    @DisplayName("should re-enable active skills after being disabled")
    void shouldRenableDisabledSkillIfActive() {

        PlayerSkill skill = player.addSkill(this.skill).playerSkill();

        skill.activate();
        assertThat(playerMock.hasPermission("foobar")).isTrue();

        skill.disable();
        assertThat(playerMock.hasPermission("foobar")).isFalse();

        skill.enable();
        assertThat(playerMock.hasPermission("foobar")).isTrue();
    }

    @Test
    @DisplayName("should not enable skills that became inactive")
    void shouldNotEnableSkillsThatWereActiveButBecameInactive() {

        PlayerSkill skill = player.addSkill(this.skill).playerSkill();

        skill.activate();
        assertThat(playerMock.hasPermission("foobar")).isTrue();

        skill.disable();
        assertThat(playerMock.hasPermission("foobar")).isFalse();

        skill.deactivate();
        skill.enable();
        assertThat(playerMock.hasPermission("foobar")).isFalse();
    }

    @Nested
    @DisplayName("Parent -> Child")
    class ParentChildSkills {

        private static final String parent = "parent";
        private static final String child1 = "parent:child1";
        private static final String child2 = "parent:child1:child2";

        @Captor
        private ArgumentCaptor<ExecutionResult> captor;

        @BeforeEach
        void setUp() {

            MockitoAnnotations.openMocks(this);
            plugin.getSkillManager().registerSkill(DefaultSkillContextTest.TestSkill.class, context -> {
                return spy(new DefaultSkillContextTest.TestSkill(context));
            });
        }

        @AfterEach
        void tearDown() {

            loadSkill(parent).delete();
        }

        private Consumer<ExecutionResult> callback() {

            Consumer<ExecutionResult> consumer = spy(new Consumer<ExecutionResult>() {
                @Override
                public void accept(ExecutionResult executionResult) {

                }
            });
            return consumer;
        }

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
            cfg.set("no-skill-slot", true);
            cfg.set("skills.child1.name", child1);
            cfg.set("skills.child1.skills.child2.name", child2);
            cfg.set("skills.child1.skills.child2.type", "test");
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
        @DisplayName("should create nested player skills")
        void shouldCreateNestedPlayerSkills() {

            ConfiguredSkill parent = loadSkill();
            PlayerSkill playerSkill = PlayerSkill.getOrCreate(player, parent);

            assertThat(playerSkill)
                    .extracting(PlayerSkill::isParent, PlayerSkill::isChild, s -> s.children().size())
                    .contains(true, false, 1);

            assertThat(PlayerSkill.find(player, getOrAssertSkill(child2)))
                    .isPresent().get()
                    .extracting(PlayerSkill::isParent, PlayerSkill::parent, PlayerSkill::isChild)
                    .contains(false, PlayerSkill.find(player, getOrAssertSkill(child1)).orElseThrow(), true);

            assertThat(PlayerSkill.find(player, getOrAssertSkill(child1)))
                    .isPresent().get()
                    .extracting(PlayerSkill::isParent, PlayerSkill::parent, PlayerSkill::isChild)
                    .contains(true, PlayerSkill.find(player, getOrAssertSkill(ParentChildSkills.parent)).orElseThrow(), true);

        }

        @Test
        @DisplayName("should add and activate all child skills")
        void shouldActivateAllSkills() {

            ConfiguredSkill parent = loadSkill();

            AddSkillAction.Result result = player.addSkill(parent);

            assertThat(result.success()).isTrue();
            assertThat(result.playerSkill().active())
                    .isTrue();
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child1)))
                    .extracting(PlayerSkill::active)
                    .isEqualTo(true);
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child2)))
                    .extracting(PlayerSkill::active)
                    .isEqualTo(true);
        }

        @Test
        @DisplayName("should not activate child skills with level requirement")
        void shouldNotActivateChildSkillsWithRequirement() {

            ConfiguredSkill parent = loadSkill(ParentChildSkills.parent, cfg -> {
                cfg.set("skills.child1.level", 5);
            });

            AddSkillAction.Result result = player.addSkill(parent);

            assertThat(result.success()).isTrue();
            assertThat(result.playerSkill().active())
                    .isTrue();
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child1)))
                    .extracting(PlayerSkill::active)
                    .isEqualTo(false);
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child2)))
                    .extracting(PlayerSkill::active)
                    .isEqualTo(false);
        }

        @Test
        @DisplayName("should not activate child if parent is not active")
        void shouldNotActivateChildSkillIfParentIsNotActivated() {

            ConfiguredSkill parent = loadSkill(ParentChildSkills.parent, cfg -> {
                cfg.set("no-skill-slot", false);
                cfg.set("auto-activate", false);
                cfg.set("skills.child1.skillpoints", 1);
            });
            player.addSkillPoints(5);

            AddSkillAction.Result result = player.addSkill(parent);

            assertThat(result.success()).isTrue();
            assertThat(result.playerSkill().active())
                    .isFalse();
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child1)))
                    .extracting(PlayerSkill::active)
                    .isEqualTo(false);
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child2)))
                    .extracting(PlayerSkill::active)
                    .isEqualTo(false);

            result = player.buySkill(getOrAssertSkill(child1));

            assertThat(result.success()).isTrue();
            assertThat(result.playerSkill().active())
                    .isFalse();
        }

        @Test
        @DisplayName("should auto activate skill when bought")
        void shouldAutoActivateSkillWhenBought() {

            ConfiguredSkill parent = loadSkill(ParentChildSkills.parent, cfg -> {
                cfg.set("no-skill-slot", false);
                cfg.set("skills.child1.skillpoints", 1);
            });
            player.addSkillPoints(5);

            AddSkillAction.Result result = player.addSkill(parent);

            assertThat(result.success()).isTrue();
            assertThat(result.playerSkill().active())
                    .isTrue();
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child1)))
                    .extracting(PlayerSkill::active)
                    .isEqualTo(false);
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child2)))
                    .extracting(PlayerSkill::active)
                    .isEqualTo(false);

            result = player.buySkill(getOrAssertSkill(child1));

            assertThat(result.success()).isTrue();
            assertThat(result.playerSkill().active())
                    .isTrue();
        }

        @Test
        @DisplayName("should not activate nested child skills with level requirement")
        void shouldNotActivateNestedChildSkillsWithRequirement() {

            ConfiguredSkill parent = loadSkill(ParentChildSkills.parent, cfg -> {
                cfg.set("skills.child1.skills.child2.skillpoints", 5);
            });

            AddSkillAction.Result result = player.addSkill(parent);

            assertThat(result.success()).isTrue();
            assertThat(result.playerSkill().active())
                    .isTrue();
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child1)))
                    .extracting(PlayerSkill::active)
                    .isEqualTo(true);
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child2)))
                    .extracting(PlayerSkill::active)
                    .isEqualTo(false);
        }

        @Test
        @DisplayName("should auto activate child skills on level up")
        void shouldAutoActivateChildSkillsOnLevelUp() {

            ConfiguredSkill parent = loadSkill(ParentChildSkills.parent, cfg -> {
                cfg.set("skills.child1.level", 5);
            });

            AddSkillAction.Result result = player.addSkill(parent);

            assertThat(result.success()).isTrue();
            assertThat(result.playerSkill().active())
                    .isTrue();
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child1)))
                    .extracting(PlayerSkill::active)
                    .isEqualTo(false);
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child2)))
                    .extracting(PlayerSkill::active)
                    .isEqualTo(false);

            result.playerSkill().player().addLevel(5);

            assertThat(result.playerSkill().active())
                    .isTrue();
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child1)))
                    .extracting(PlayerSkill::active)
                    .isEqualTo(true);
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child2)))
                    .extracting(PlayerSkill::active)
                    .isEqualTo(true);
        }

        @Test
        @DisplayName("should allow buying child skills")
        void shouldAllowBuyingChildSkills() {

            ConfiguredSkill parent = loadSkill(ParentChildSkills.parent, cfg -> {
                cfg.set("skills.child1.money", 500);
            });

            AddSkillAction.Result result = player.buySkill(parent);

            assertThat(result.success()).isTrue();
            assertThat(result.playerSkill().active())
                    .isTrue();
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child1)))
                    .extracting(PlayerSkill::active)
                    .isEqualTo(false);
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child2)))
                    .extracting(PlayerSkill::active)
                    .isEqualTo(false);

            player.buySkill(getOrAssertSkill(child1), true);

            assertThat(result.playerSkill().active())
                    .isTrue();
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child1)))
                    .extracting(PlayerSkill::active)
                    .isEqualTo(true);
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child2)))
                    .extracting(PlayerSkill::active)
                    .isEqualTo(true);
        }

        @Test
        @DisplayName("should execute executable child skills")
        void shouldExecuteExecutableChildSkills() {

            ConfiguredSkill skill = loadSkill();

            AddSkillAction.Result result = player.addSkill(skill);

            assertThat(result.success()).isTrue();
            assertThat(result.playerSkill().active())
                    .isTrue();
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child1)))
                    .extracting(PlayerSkill::active)
                    .isEqualTo(true);
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child2)))
                    .extracting(PlayerSkill::active)
                    .isEqualTo(true);

            Consumer<ExecutionResult> callback = callback();
            result.playerSkill().execute(callback);

            verify(callback, times(1)).accept(captor.capture());

            assertThat(captor.getValue())
                    .extracting(ExecutionResult::success)
                    .isEqualTo(true);
        }

        @Test
        @DisplayName("should not execute disabled skills")
        void shouldNotExecuteDisabledSkills() {

            ConfiguredSkill skill = loadSkill(parent, cfg -> {
                cfg.set("skills.child1.disable-parent", true);
                cfg.set("type", "test");
                cfg.set("skills.child1.type", "none");
                cfg.set("skills.child1.skills.child2.type", "none");
            });

            AddSkillAction.Result result = player.addSkill(skill);

            assertThat(result.success()).isTrue();
            PlayerSkill playerSkill = result.playerSkill();
            assertThat(playerSkill.active())
                    .isTrue();
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child1)))
                    .extracting(PlayerSkill::active)
                    .isEqualTo(true);
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child2)))
                    .extracting(PlayerSkill::active)
                    .isEqualTo(true);

            playerSkill.refresh();
            assertThat(playerSkill.replaced()).isTrue();

            Consumer<ExecutionResult> callback = callback();
            playerSkill.execute(callback);

            verify(callback, never()).accept(captor.capture());
        }

        @Test
        @DisplayName("should disable parent if child sets disables list")
        void disablesParentSkillIfChildSetsDisableParent() {

            ConfiguredSkill skill = loadSkill(parent, cfg -> {
                cfg.set("skills.child1.disable-parent", true);
                cfg.set("skills.child1.skillpoints", 1);
            });

            AddSkillAction.Result result = player.addSkill(skill);

            assertThat(result.success()).isTrue();
            PlayerSkill playerSkill = result.playerSkill();
            assertThat(playerSkill.active())
                    .isTrue();
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child1)))
                    .extracting(PlayerSkill::active)
                    .isEqualTo(false);
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child2)))
                    .extracting(PlayerSkill::active)
                    .isEqualTo(false);
            assertThat(playerSkill.replaced()).isFalse();

            player.addSkill(getOrAssertSkill(child1), true);

            playerSkill.refresh();
            assertThat(playerSkill.replaced())
                    .isTrue();
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child1)).replaced())
                    .isFalse();

        }

        @Test
        @DisplayName("should enable child skill if parent is disabled")
        void shouldEnableChildSkillEventIfParentIsDisabled() {

            ConfiguredSkill skill = loadSkill(parent, cfg -> {
                cfg.set("skills.child1.type", "permission");
                cfg.set("skills.child1.with.permissions", Arrays.asList("foobar"));
                cfg.set("skills.child1.disable-parent", true);
            });

            AddSkillAction.Result result = player.addSkill(skill, true);
            assertThat(result.success()).isTrue();

            assertThat(result.playerSkill())
                    .extracting(PlayerSkill::active, PlayerSkill::disabled)
                    .contains(true, true);
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child1)))
                    .extracting(PlayerSkill::active, PlayerSkill::enabled)
                    .contains(true, true);

            assertThat(playerMock.hasPermission("foobar")).isTrue();
        }


        @Test
        @DisplayName("should not enable skill if world is disabled")
        void shouldNotEnableSkillIfWorldIsDisabled() {

            ConfiguredSkill skill = loadSkill(parent, cfg -> {
                cfg.set("type", "permission");
                cfg.set("with.permissions", Arrays.asList("foobar"));
                cfg.set("disabled-worlds", Arrays.asList("world"));
            });

            Location location = new Location(server.addSimpleWorld("world"), 0, 0, 0);
            playerMock.setLocation(location);
            playerMock.assertLocation(location, 0);

            AddSkillAction.Result result = player.addSkill(skill, true);

            assertThat(result.success()).isTrue();
            assertThat(playerMock.hasPermission("foobar")).isFalse();

            location = new Location(server.addSimpleWorld("foobar"), 0, 0, 0);
            playerMock.teleport(location);
            playerMock.assertLocation(location, 0);

            result.playerSkill().enable();
            assertThat(playerMock.hasPermission("foobar")).isTrue();
        }

        @Test
        @DisplayName("should enable skill if player is in skill enabled world")
        void shouldEnableSkillInEnabledWorlds() {

            ConfiguredSkill skill = loadSkill(parent, cfg -> {
                cfg.set("type", "permission");
                cfg.set("with.permissions", Arrays.asList("foobar"));
                cfg.set("worlds", Arrays.asList("world"));
            });

            Location location = new Location(server.addSimpleWorld("world"), 0, 0, 0);
            playerMock.setLocation(location);
            playerMock.assertLocation(location, 0);

            AddSkillAction.Result result = player.addSkill(skill, true);

            assertThat(result.success()).isTrue();
            assertThat(playerMock.hasPermission("foobar")).isTrue();

            location = new Location(server.addSimpleWorld("foobar"), 0, 0, 0);
            playerMock.setLocation(location);
            playerMock.assertLocation(location, 0);

            result.playerSkill().enable();
            assertThat(playerMock.hasPermission("foobar")).isFalse();
        }

        @Test
        @DisplayName("should replace parent skill in slot")
        void shouldReplaceParentSkillSlot() {

            ConfiguredSkill skill = loadSkill(parent, cfg -> {
                cfg.set("no-skill-slot", false);
                cfg.set("skills.child1.replace-parent", true);
                cfg.set("skills.child1.skillpoints", 1);
            });

            AddSkillAction.Result result = player.addSkill(skill, true);
            assertThat(result.success()).isTrue();

            ConfiguredSkill childSkill = getOrAssertSkill(child1);
            PlayerSkill childPlayerSkill = PlayerSkill.getOrCreate(player, childSkill);

            assertThat(result.playerSkill().active()).isTrue();
            assertThat(childPlayerSkill.active()).isFalse();

            assertThat(SkillSlot.of(result.playerSkill())).isPresent();
            assertThat(SkillSlot.of(childPlayerSkill)).isEmpty();

            player.addSkill(childSkill, true);

            result.playerSkill().refresh();
            childPlayerSkill.refresh();

            assertThat(result.playerSkill().replaced()).isTrue();
            assertThat(childPlayerSkill.active()).isTrue();

            assertThat(SkillSlot.of(result.playerSkill())).isEmpty();
            assertThat(SkillSlot.of(childPlayerSkill)).isPresent();
        }

        @Test
        @DisplayName("resetSkillSlots() should reset and deactivate replaced parent skills")
        void shouldResetParentSkills() {

            ConfiguredSkill skill = loadSkill(parent, cfg -> {
                cfg.set("type", "none");
                cfg.set("no-skill-slot", false);
                cfg.set("skills.child1.replace-parent", true);
                cfg.set("skills.child1.type", "none");
                cfg.set("skills.child1.skills.child2.type", "none");
                cfg.set("skills.child1.skills.child2.replace-parent", true);
            });

            AddSkillAction.Result result = player.addSkill(skill);

            assertThat(result.success()).isTrue();
            PlayerSkill playerSkill = result.playerSkill();
            assertThat(playerSkill)
                    .extracting(PlayerSkill::active, PlayerSkill::replaced)
                    .contains(true, true);
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child1)))
                    .extracting(PlayerSkill::active, PlayerSkill::replaced)
                    .contains(true, true);
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child2)))
                    .extracting(PlayerSkill::active, PlayerSkill::replaced)
                    .contains(true, false);

            player.refresh();
            List<PlayerSkill> skills = player.resetSkillSlots();
            playerSkill.refresh();

            assertThat(playerSkill)
                    .extracting(PlayerSkill::active, PlayerSkill::replaced)
                    .contains(false, false);
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child1)))
                    .extracting(PlayerSkill::active, PlayerSkill::replaced)
                    .contains(false, false);
            assertThat(PlayerSkill.getOrCreate(player, getOrAssertSkill(ParentChildSkills.child2)))
                    .extracting(PlayerSkill::active, PlayerSkill::replaced)
                    .contains(false, false);
        }
    }
}
