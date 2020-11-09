package net.silthus.skills;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.silthus.ebean.BaseEntity;
import net.silthus.ebean.Config;
import net.silthus.ebean.EbeanWrapper;
import net.silthus.skills.entities.SkilledPlayer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SkillManagerTest {

    private ServerMock server;
    private SkillManager skillManager;

    @BeforeEach
    void setUp() {

        this.server = MockBukkit.mock();
        this.skillManager = new SkillManager(new EbeanWrapper(Config.builder().build()).getDatabase());
    }

    @AfterEach
    void tearDown() {

        MockBukkit.unmock();
    }

    @Nested
    @DisplayName("getPlayer(...)")
    class getPlayer {

        @Test
        @DisplayName("should always return a player regardless of it exists in the db")
        void shouldAlwaysReturnAPlayer() {

            PlayerMock player = server.addPlayer();
            assertThat(skillManager.getPlayer(player))
                    .isNotNull()
                    .extracting(BaseEntity::id, SkilledPlayer::name)
                    .contains(player.getUniqueId(), player.getName());
        }
    }
}