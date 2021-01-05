package de.raidcraft.skills.entities;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import de.raidcraft.skills.DefaultSkillContextTest;
import de.raidcraft.skills.ExecutionResult;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.actions.AddSkillAction;
import de.raidcraft.skills.util.RandomString;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("ALL")
public class PlayerSkillTest {

    private static final RandomString rnd = new RandomString();
    private String TEST_SKILL = "test";

    private ServerMock server;
    private SkillsPlugin plugin;

    private SkilledPlayer player;

    private ConfiguredSkill skill;

    @BeforeEach
    void setUp() {

        server = MockBukkit.mock();
        plugin = MockBukkit.load(SkillsPlugin.class);

        TEST_SKILL = rnd.nextString();
        MemoryConfiguration cfg = new MemoryConfiguration();
        cfg.set("type", "permission");
        cfg.set("name", "Test Skill");
        cfg.set("with.permissions", Collections.singletonList("foobar"));
        plugin.getSkillManager().loadSkill(TEST_SKILL, cfg);

        PlayerMock playerMock = server.addPlayer(rnd.nextString());
        playerMock.setOp(true);
        this.player = SkilledPlayer.getOrCreate(playerMock);
        player.addSkillSlots(20, SkillSlot.Status.FREE);
        skill = ConfiguredSkill.findByAliasOrName(TEST_SKILL).get();
    }

    @AfterEach
    void tearDown() {

        PlayerSkill.find.all().stream().filter(skill1 -> !skill1.isChild()).forEach(PlayerSkill::delete);
        ConfiguredSkill.find.all().stream().filter(skill1 -> !skill1.isChild()).forEach(ConfiguredSkill::delete);
        SkilledPlayer.find.all().forEach(SkilledPlayer::delete);

        MockBukkit.unmock();
    }

    @Test
    @DisplayName("should activate skill if all requirements are met")
    void shouldActivateSkill() {

        player.addSkill(skill).playerSkill().activate();

        assertThat(PlayerSkill.getOrCreate(player, skill))
                .extracting(PlayerSkill::active)
                .isEqualTo(true);
    }

    @Test
    @DisplayName("should not activate disabled skills")
    void shouldNotActivateSkillThatIsDisabled() {

        skill.enabled(false).save();
        PlayerSkill playerSkill = player.addSkill(skill).playerSkill();
        playerSkill.activate();

        assertThat(playerSkill.active()).isFalse();
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
    }
}
