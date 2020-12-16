package net.silthus.skills.entities;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import net.silthus.skills.SkillsPlugin;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collections;

public class PlayerSkillTest {

    private static final String TEST_SKILL = "test";

    private ServerMock server;
    private SkillsPlugin plugin;

    private SkilledPlayer player;

    private ConfiguredSkill skill;
    @BeforeEach
    void setUp() {

        server = MockBukkit.mock();
        plugin = MockBukkit.load(SkillsPlugin.class);
        MemoryConfiguration cfg = new MemoryConfiguration();
        cfg.set("type", "permission");
        cfg.set("name", "Test Skill");
        cfg.set("with.permissions", Collections.singletonList("foobar"));
        plugin.getSkillManager().loadSkill(TEST_SKILL, cfg);

        player = SkilledPlayer.getOrCreate(server.addPlayer());
        skill = ConfiguredSkill.findByAliasOrName(TEST_SKILL).get();
    }

    @AfterEach
    void tearDown() {

        MockBukkit.unmock();
    }
}
