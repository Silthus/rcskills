package de.raidcraft.skills;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import be.seeseemelk.mockbukkit.scheduler.BukkitSchedulerMock;
import de.raidcraft.skills.entities.ConfiguredSkill;
import de.raidcraft.skills.entities.PlayerSkill;
import de.raidcraft.skills.entities.SkilledPlayer;
import de.raidcraft.skills.util.RandomString;
import lombok.SneakyThrows;
import mockit.Mock;
import mockit.MockUp;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("ALL")
public
class DefaultSkillContextTest {

    private String skillAlias;
    private ServerMock server;
    private RCSkills plugin;
    private BukkitSchedulerMock scheduler;
    private ConfiguredSkill configuredSkill;
    private PlayerMock playerMock;
    private SkilledPlayer player;
    private ConfigurationSection config;

    @BeforeEach
    void setUp() {
        this.skillAlias = new RandomString().nextString();
        this.server = MockBukkit.mock(new de.raidcraft.skills.ServerMock());
        this.plugin = MockBukkit.load(RCSkills.class);
        this.scheduler = server.getScheduler();
        this.config = new MemoryConfiguration();

        plugin.getSkillManager().registerSkill(TestSkill.class, context -> {
            return spy(new TestSkill(context));
        });

        this.configuredSkill = ConfiguredSkill.getOrCreate(UUID.randomUUID())
                .alias(skillAlias)
                .type("test")
                .autoUnlock(true)
                .noSkillSlot(true);
        this.playerMock = server.addPlayer();
        this.player = SkilledPlayer.getOrCreate(playerMock);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private ConfiguredSkill load(Consumer<ConfiguredSkill> skill) {

        skill.accept(this.configuredSkill);
        this.configuredSkill.load(config);
        return this.configuredSkill;
    }

    private PlayerSkill loadAndAdd() {

        return loadAndAdd(skill -> {});
    }

    private PlayerSkill loadAndAdd(Consumer<ConfiguredSkill> skill) {

        return player.addSkill(load(skill), true).playerSkill();
    }

    private SkillContext loadContext() {

        return loadContext(skill -> {});
    }

    private SkillContext loadContext(Consumer<ConfiguredSkill> skill) {

        return plugin.getSkillManager().loadSkill(loadAndAdd(skill));
    }

    @Test
    @DisplayName("should load the configured skill")
    void shouldLoadSkill() {

        load(skill -> skill.name("Test Skill"));

        assertThat(ConfiguredSkill.findByAliasOrName(skillAlias))
                .isPresent()
                .get().extracting(ConfiguredSkill::name)
                .isEqualTo("Test Skill");
    }

    @Test
    @DisplayName("should add skill to player")
    void shouldAddSkillToPlayer() {

        PlayerSkill playerSkill = loadAndAdd();

        assertThat(player.getSkill(skillAlias))
                .isPresent()
                .get().extracting(PlayerSkill::active, PlayerSkill::unlocked)
                .contains(true, true);
    }

    @Test
    @DisplayName("should call apply on test skill")
    void shouldCallApply() {

        verify(loadContext().get(), times(1)).apply();
    }

    @Test
    @DisplayName("should call remove when skill is deactivated")
    void shouldCallRemove() {

        SkillContext skillContext = loadContext();

        verify(skillContext.get(), never()).remove();

        skillContext.playerSkill().deactivate();

        verify(skillContext.get(), times(1)).remove();
    }

    @Nested
    @DisplayName("Executable Skill")
    class ExecutableSkill {

        @Captor
        private ArgumentCaptor<ExecutionResult> captor;

        @BeforeEach
        void setUp() {

            MockitoAnnotations.openMocks(this);
        }

        private Consumer<ExecutionResult> callback() {

            Consumer<ExecutionResult> consumer = spy(new Consumer<ExecutionResult>() {
                @Override
                public void accept(ExecutionResult executionResult) {

                }
            });
            return consumer;
        }

        @SneakyThrows
        @Test
        @DisplayName("should execute skill after delay")
        void shouldExecuteSkillAfterDelay() {

            config.set("execution.delay", "5");
            SkillContext skillContext = loadContext();

            skillContext.execute(executionResult -> assertThat(executionResult.success()));

            verify((Executable) skillContext.get(), never()).execute(any());

            scheduler.performTicks(5L);

            verify((Executable) skillContext.get(), times(1)).execute(any());
        }

        @Test
        @DisplayName("should not execute skill on cooldown")
        void shouldNotExecuteSkillThatIsOnCooldown() {

            config.set("execution.cooldown", "10s");
            SkillContext skillContext = loadContext();

            Consumer<ExecutionResult> callback = callback();
            skillContext.execute(callback);

            verify(callback).accept(captor.capture());
            assertThat(captor.getValue())
                    .extracting(ExecutionResult::success)
                    .isEqualTo(true);

            Consumer<ExecutionResult> secondCallback = callback();
            skillContext.execute(secondCallback);
            verify(secondCallback).accept(captor.capture());
            assertThat(captor.getValue())
                    .extracting(ExecutionResult::status)
                    .isEqualTo(ExecutionResult.Status.COOLDOWN);
        }

        @Test
        @DisplayName("should execute skill after cooldown is over")
        void shouldExecuteSkillAfterCooldownEnds() {

            config.set("execution.cooldown", "10s");
            SkillContext skillContext = loadContext();

            Consumer<ExecutionResult> callback = callback();
            skillContext.execute(callback);

            verify(callback).accept(captor.capture());
            assertThat(captor.getValue())
                    .extracting(ExecutionResult::success)
                    .isEqualTo(true);

            Clock clock = Clock.fixed(Instant.now().plus(10, ChronoUnit.SECONDS), ZoneOffset.UTC);
            new MockUp<Instant>() {
                @Mock
                public Instant now() {
                    return Instant.now(clock);
                }
            };

            Consumer<ExecutionResult> secondCallback = callback();
            skillContext.execute(secondCallback);
            verify(secondCallback).accept(captor.capture());
            assertThat(captor.getValue())
                    .extracting(ExecutionResult::status)
                    .isEqualTo(ExecutionResult.Status.SUCCESS);
        }
    }

    @SkillInfo("test")
    public static class TestSkill extends AbstractSkill implements Executable {

        public TestSkill(SkillContext context) {
            super(context);
        }

        @Override
        public void load(ConfigurationSection config) {

        }

        @Override
        public void apply() {

        }

        @Override
        public void remove() {

        }

        @Override
        public ExecutionResult execute(ExecutionContext context) throws Exception {

            return success(context);
        }
    }
}