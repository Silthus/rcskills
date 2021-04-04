package de.raidcraft.skills.plan;

import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.annotation.NumberProvider;
import com.djrapitops.plan.extension.annotation.PluginInfo;
import com.djrapitops.plan.extension.annotation.StringProvider;
import com.djrapitops.plan.extension.annotation.TableProvider;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.table.Table;
import de.raidcraft.skills.entities.ConfiguredSkill;
import de.raidcraft.skills.entities.Level;
import de.raidcraft.skills.entities.PlayerSkill;
import de.raidcraft.skills.entities.SkilledPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@PluginInfo(
        name = "RCSkills",
        iconName = "khanda",
        iconFamily = Family.SOLID,
        color = Color.DEEP_ORANGE
)
public class RCSkillsDataExtension implements DataExtension {

    @Override
    public CallEvents[] callExtensionMethodsOn() {
        return new CallEvents[]{
                CallEvents.PLAYER_JOIN,
                CallEvents.PLAYER_LEAVE,
                CallEvents.SERVER_EXTENSION_REGISTER
        };
    }

    @TableProvider()
    public Table skilledPlayerStats() {

        Table.Factory factory = Table.builder()
                .columnOne("Player", Icon.called("user-ninja").of(Color.DEEP_ORANGE).of(Family.SOLID).build())
                .columnTwo("Level", Icon.called("angle-double-up").of(Color.DEEP_ORANGE).of(Family.SOLID).build())
                .columnThree("EXP", Icon.called("head-wizard").of(Color.DEEP_ORANGE).of(Family.SOLID).build())
                .columnFour("Skill Points", Icon.called("galactic-republic").of(Color.LIGHT_GREEN).of(Family.SOLID).build())
                .columnFive("Skill Slots (Σ Skills)", Icon.called("vector-square").of(Color.BLACK).of(Family.SOLID).build());

        List<SkilledPlayer> players = SkilledPlayer.find.query()
                .orderBy().desc("level.total_exp")
                .orderBy().desc("level.level")
                .findList();

        for (SkilledPlayer player : players) {
            factory.addRow(
                    player.name(),
                    player.level().getLevel(),
                    player.level().getTotalExp(),
                    player.skillPoints(),
                    player.activeSlotCount() + "/" + player.slotCount() + " (" + player.skillCount() + ")"
            );
        }

        return factory.build();
    }

    @TableProvider()
    public Table skillStats() {

        Table.Factory factory = Table.builder()
                .columnOne("Skill", Icon.called("book-dead").of(Color.AMBER).of(Family.SOLID).build())
                .columnTwo("Level", Icon.called("angle-double-up").of(Color.DEEP_ORANGE).of(Family.SOLID).build())
                .columnThree("Players Bought", Icon.called("users").of(Color.LIGHT_GREEN).of(Family.SOLID).build())
                .columnFour("Players Active", Icon.called("check-square").of(Color.GREEN).of(Family.REGULAR).build());

        for (ConfiguredSkill skill : ConfiguredSkill.allEnabled()
                .stream().sorted(Comparator.comparingInt(ConfiguredSkill::level))
                .collect(Collectors.toList())) {
            List<PlayerSkill> playerSkills = skill.playerSkills();
            factory.addRow(
                    skill.name() + " (" + skill.alias() + ")",
                    skill.level(),
                    playerSkills.stream().filter(PlayerSkill::unlocked).count(),
                    playerSkills.stream().filter(playerSkill -> !playerSkill.replaced()).filter(PlayerSkill::active).count()
            );
        }


        return factory.build();
    }

    @NumberProvider(
            text = "RC-Level",
            description = "The RC-Level of the player",
            priority = 2,
            iconName = "angle-double-up",
            iconFamily = Family.SOLID,
            iconColor = Color.DEEP_ORANGE,
            showInPlayerTable = true
    )
    public long level(UUID playerUUID) {

        return Optional.ofNullable(SkilledPlayer.find.byId(playerUUID))
                .map(SkilledPlayer::level)
                .map(Level::getLevel)
                .orElse(0);
    }

    @NumberProvider(
            text = "RC-EXP",
            description = "The RC-EXP of the player",
            priority = 3,
            iconName = "head-wizard",
            iconFamily = Family.SOLID,
            iconColor = Color.DEEP_ORANGE,
            showInPlayerTable = true
    )
    public long totalExp(UUID playerUUID) {

        return Optional.ofNullable(SkilledPlayer.find.byId(playerUUID))
                .map(SkilledPlayer::level)
                .map(Level::getTotalExp)
                .orElse(0L);
    }

    @NumberProvider(
            text = "Skillpoints",
            description = "The skillpoints of the player.",
            priority = 3,
            iconName = "galactic-republic",
            iconFamily = Family.SOLID,
            iconColor = Color.DEEP_ORANGE
    )
    public long skillpoints(UUID playerUUID) {

        return Optional.ofNullable(SkilledPlayer.find.byId(playerUUID))
                .map(SkilledPlayer::skillPoints)
                .orElse(0);
    }

    @NumberProvider(
            text = "Free Skill Slots",
            description = "The free skill slots of the player.",
            priority = 4,
            iconName = "square",
            iconFamily = Family.REGULAR,
            iconColor = Color.GREY
    )
    public long freeSkillSlots(UUID playerUUID) {

        return Optional.ofNullable(SkilledPlayer.find.byId(playerUUID))
                .map(SkilledPlayer::freeSkillSlots)
                .orElse(0);
    }

    @NumberProvider(
            text = "Skill Slots",
            description = "The number of skill slots for the player.",
            priority = 3,
            iconName = "vector-square",
            iconFamily = Family.REGULAR,
            iconColor = Color.BLACK
    )
    public long totalSkillSlots(UUID playerUUID) {

        return Optional.ofNullable(SkilledPlayer.find.byId(playerUUID))
                .map(SkilledPlayer::slotCount)
                .orElse(0);
    }

    @NumberProvider(
            text = "Active Skills",
            description = "The number of active skills.",
            priority = 3,
            iconName = "book-dead",
            iconFamily = Family.SOLID,
            iconColor = Color.AMBER
    )
    public long activeSkillsCount(UUID playerUUID) {

        return Optional.ofNullable(SkilledPlayer.find.byId(playerUUID))
                .map(SkilledPlayer::activeSlotCount)
                .orElse(0);
    }

    @StringProvider(
            text = "Active Skills",
            description = "The active skills of the player.",
            priority = 4,
            iconName = "book-dead",
            iconFamily = Family.SOLID,
            iconColor = Color.AMBER,
            showInPlayerTable = true
    )
    public String activeSkills(UUID playerUUID) {

        return Optional.ofNullable(SkilledPlayer.find.byId(playerUUID))
                .map(SkilledPlayer::activeSkills)
                .map(playerSkills -> playerSkills.stream()
                        .map(PlayerSkill::configuredSkill)
                        .sorted(Comparator.comparingInt(ConfiguredSkill::level))
                        .map(ConfiguredSkill::name)
                        .collect(Collectors.joining(","))
                )
                .orElse(null);
    }
}
