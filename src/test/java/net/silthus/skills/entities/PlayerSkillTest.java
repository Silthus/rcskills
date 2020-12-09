package net.silthus.skills.entities;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import net.silthus.skills.SkillsPlugin;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collections;

public class PlayerSkillTest {

    private static final String TEST_SKILL = "test";

    private static ServerMock server;
    private static SkillsPlugin plugin;

    @BeforeAll
    static void beforeAll() {

        server = MockBukkit.mock();
        plugin = MockBukkit.load(SkillsPlugin.class);
        MemoryConfiguration cfg = new MemoryConfiguration();
        cfg.set("type", "permission");
        cfg.set("name", "Test Skill");
        cfg.set("with.permissions", Collections.singletonList("foobar"));
        plugin.getSkillManager().loadSkill(TEST_SKILL, cfg);
    }

    @AfterAll
    static void tearDown() {

        MockBukkit.unmock();
    }

    private SkilledPlayer player;
    private ConfiguredSkill skill;

    @BeforeEach
    void setUp() {

        player = SkilledPlayer.getOrCreate(server.addPlayer());
        skill = ConfiguredSkill.findByAliasOrName(TEST_SKILL).get();
    }
}
