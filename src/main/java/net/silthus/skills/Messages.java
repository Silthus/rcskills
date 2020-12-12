package net.silthus.skills;

import lombok.AccessLevel;
import lombok.Setter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.TextColor;
import net.silthus.skills.entities.PlayerLevel;
import net.silthus.skills.entities.PlayerSkill;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

public final class Messages {

    public static void send(UUID playerId, Component message) {
        if (SkillsPlugin.isTesting()) return;
        BukkitAudiences.create(SkillsPlugin.instance())
                .player(playerId)
                .sendMessage(message);
    }

    public static void send(UUID playerId, Consumer<TextComponent.Builder> message) {

        TextComponent.Builder builder = text();
        message.accept(builder);
        send(playerId, builder.build());
    }

    public static void send(Player player, Component message) {
        send(player.getUniqueId(), message);
    }

    public static Component addSkill(SkilledPlayer player, PlayerSkill skill) {

        return text().append(player(player))
                .append(text(" hat den Skill ", GREEN))
                .append(skill(skill))
                .append(text(" erhalten.", GREEN)).build();
    }

    public static Component removeSkill(PlayerSkill playerSkill) {

        return text("Der Skill ", RED)
                .append(skill(playerSkill))
                .append(text(" wurde von ", RED))
                .append(player(playerSkill.player()))
                .append(text(" entfernt.", RED));
    }

    public static Component addSkillpoints(PlayerLevel playerLevel, int skillpoints) {

        return text("Die Skillpunkte von ", YELLOW)
                .append(player(playerLevel.player()))
                .append(text(" wurden um ", YELLOW))
                .append(text(skillpoints, AQUA))
                .append(text(" Skillpunkt(e) auf ", YELLOW))
                .append(text(playerLevel.skillPoints(), AQUA))
                .append(text(" erhöht.", YELLOW));
    }

    public static Component setSkillpoints(PlayerLevel playerLevel, int skillpoints) {

        return text("Die Skillpunkte von ", YELLOW)
                .append(player(playerLevel.player()))
                .append(text(" wurden auf ", YELLOW))
                .append(text(skillpoints, AQUA))
                .append(text(" Skillpunkt(e) gesetzt.", YELLOW));
    }

    public static Component addExp(PlayerLevel playerLevel, int exp) {

        return text("Die Erfahrungspunkte von ", YELLOW)
                .append(player(playerLevel.player()))
                .append(text(" wurden um ", YELLOW))
                .append(text(exp + " EXP", AQUA)).append(text(" auf ", YELLOW))
                .append(text(playerLevel.totalExp() + " EXP", AQUA))
                .append(text(" erhöht.", YELLOW));
    }

    public static Component setExp(PlayerLevel playerLevel, int exp) {

        return text("Die Erfahrungspunkte von ", YELLOW)
                .append(player(playerLevel.player()))
                .append(text(" wurden auf ", YELLOW))
                .append(text(exp + " EXP", AQUA))
                .append(text(" gesetzt.", YELLOW));
    }

    public static Component addLevel(PlayerLevel playerLevel, int level) {

        return text("Das Level von ", YELLOW)
                .append(player(playerLevel.player()))
                .append(text(" wurde um ", YELLOW))
                .append(text(level, AQUA)).append(text(" Level auf ", YELLOW))
                .append(text(playerLevel.level(), AQUA))
                .append(text(" erhöht.", YELLOW));
    }

    public static Component setLevel(PlayerLevel playerLevel, int level) {

        return text("Das Level von ", YELLOW)
                .append(player(playerLevel.player()))
                .append(text(" wurde auf ", YELLOW))
                .append(text("Level " + level, AQUA))
                .append(text(" gesetzt.", YELLOW));
    }

    public static Component levelUpSelf(PlayerLevel playerLevel, int level) {

        return text().append(text("Du", GOLD, BOLD).hoverEvent(playerInfo(playerLevel.player())))
                .append(text(" bist im Level aufgestiegen: ", GREEN))
                .append(text("Level " + level, AQUA))
                .append(text(" erreicht.", GREEN)).build();
    }

    public static Component levelDownSelf(PlayerLevel playerLevel, int level) {

        return text().append(text("Du", GOLD, BOLD).hoverEvent(playerInfo(playerLevel.player())))
                .append(text(" bist im Level abgestiegen: ", RED))
                .append(text("Level " + level, AQUA)).build();
    }

    public static Component levelUp(PlayerLevel level) {

        return text().append(player(level.player()))
                .append(text(" ist im Level aufgestiegen: ", GREEN))
                .append(text("Level " + level.level(), AQUA))
                .append(text(" erreicht.", GREEN)).build();
    }

    public static Component levelDown(PlayerLevel level) {

        return text().append(player(level.player()))
                .append(text(" ist im Level abgestiegen: ", RED))
                .append(text("Level " + level.level(), AQUA)).build();
    }

    public static Component level(PlayerLevel level) {

        return text("Level: ", YELLOW).append(text(level.level(), AQUA)).append(newline())
                .append(text("EXP: ", YELLOW)).append(text(level.totalExp(), AQUA)).append(newline())
                .append(text("Skillpunkte: ", YELLOW)).append(text(level.skillPoints(), AQUA));
    }

    public static Component player(SkilledPlayer player) {

        return text(player.name(), GOLD, BOLD)
                .hoverEvent(showText(playerInfo(player)));
    }

    public static Component playerInfo(SkilledPlayer player) {

        Set<PlayerSkill> skills = player.skills();
        long unlockedSkills = skills.stream().filter(PlayerSkill::unlocked).count();
        long activeSkills = skills.stream().filter(PlayerSkill::active).count();
        return text().append(text("--- [ ", DARK_AQUA))
                .append(text(player.name(), GOLD))
                .append(text(" ] ---", DARK_AQUA)).append(newline())
                .append(level(player.level())).append(newline())
                .append(text("Skills: ", YELLOW))
                .append(text(activeSkills, GREEN)).append(text("/", YELLOW)).append(text(unlockedSkills, DARK_GREEN))
                .build();
    }

    public static Component skill(PlayerSkill skill) {


        return text(skill.name(), skill.active() ? GREEN : RED, BOLD)
                .hoverEvent(skillInfo(skill));
    }

    public static Component skillInfo(PlayerSkill skill) {

        return text("--- [ ", DARK_AQUA)
                .append(text(skill.name(), skillColor(skill), BOLD))
                .append(text(" (" + skill.alias() + ")", GRAY, ITALIC))
                .append(text(" ] ---", DARK_AQUA)).append(newline())
                .append(text(skill.description(), GRAY, ITALIC)).append(newline()).append(newline())
                .append(text("Vorraussetzungen:", YELLOW)).append(newline())
                .append(requirements(skill));
    }

    public static Component requirements(PlayerSkill skill) {

        TextComponent.Builder text = text();
        for (Requirement requirement : skill.skill().requirements()) {
            text.append(text(" - ", YELLOW))
                    .append(text(requirement.name(), requirementColor(requirement, skill.player()), BOLD))
                    .hoverEvent(showText(requirement(requirement, skill.player())))
                    .append(newline());
        }
        return text.build();
    }

    public static Component requirement(Requirement requirement, SkilledPlayer player) {

        return text("--- [ ", AQUA).append(text(requirement.name(), requirementColor(requirement, player)))
                .append(newline())
                .append(text(requirement.description(), GRAY, ITALIC));
    }

    public static TextColor requirementColor(Requirement requirement, SkilledPlayer player) {

        if (player == null) return YELLOW;
        return requirement.test(player).success() ? GREEN : RED;
    }

    public static TextReplacementConfig replacePlayer(SkilledPlayer player) {

        return TextReplacementConfig.builder()
                .matchLiteral("{player}").replacement(player(player))
                .matchLiteral("{player_name}").replacement(player.name())
                .matchLiteral("{player_id}").replacement(player.id().toString())
                .matchLiteral("{skills_count}").replacement(player.skills().size() + "")
                .build();
    }

    public static TextReplacementConfig replaceLevel(PlayerLevel level) {

        return TextReplacementConfig.builder()
                .matchLiteral("{level}").replacement(level(level)).build();
    }

    public static TextReplacementConfig replaceSkill(PlayerSkill skill) {

        return TextReplacementConfig.builder()
                .matchLiteral("{skill}").replacement(skillInfo(skill))
                .matchLiteral("{skill_name}").replacement(skill.name())
                .matchLiteral("{skill_alias}").replacement(skill.alias())
                .build();
    }

    public static TextColor skillColor(PlayerSkill skill) {

        switch (skill.status()) {
            case ACTIVE:
                return GREEN;
            case UNLOCKED:
                return DARK_GREEN;
            case REMOVED:
                return DARK_GRAY;
            case INACTIVE:
                return GRAY;
            case DISABLED:
                return DARK_RED;
            default:
                return WHITE;
        }
    }

    @Setter(AccessLevel.PACKAGE)
    private static Messages instance;

    public static String msg(String key) {

        return msg(key, "");
    }

    public static String msg(String key, String defaultValue) {
        if (instance == null) {
            return "";
        }
        return instance.getMessage(key, defaultValue);
    }
    private final File file;

    private final YamlConfiguration config;

    Messages(File file) throws InvalidConfigurationException {

        try {
            this.file = file;
            this.config = new YamlConfiguration();
            this.config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            throw new InvalidConfigurationException(e);
        }
    }

    public String getMessage(String key) {

        return getMessage(key, "");
    }

    public String getMessage(String key, String defaultValue) {

        if (!config.contains(key)) {
            try {
                config.set(key, defaultValue);
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return config.getString(key, defaultValue);
    }
}
