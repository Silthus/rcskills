package net.silthus.skills;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Setter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.silthus.skills.entities.ConfiguredSkill;
import net.silthus.skills.entities.PlayerLevel;
import net.silthus.skills.entities.PlayerSkill;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

public final class Messages {

    public static void send(UUID playerId, Component message) {
        BukkitAudiences.create(SkillsPlugin.instance())
                .player(playerId)
                .sendMessage(message);
    }

    public static void send(UUID playerId, Consumer<TextComponent.Builder> message) {

        TextComponent.Builder builder = text();
        message.accept(builder);
        send(playerId, builder.build());
    }

    public static TextComponent addSkill(SkilledPlayer player, PlayerSkill skill) {

        return text().append(text(player.name(), GOLD, BOLD)).hoverEvent(showText(player(player)))
                .append(text(" hat den Skill ", YELLOW))
                .append(text(skill.skill().name(), GREEN, BOLD)).hoverEvent(showText(skill(skill)))
                .append(text(" erhalten.", YELLOW)).build();
    }

    public static TextComponent addSkillpoints(PlayerLevel playerLevel, int skillpoints) {

        return text("Die ", YELLOW)
                .append(text("Skillpunkte", AQUA, BOLD).hoverEvent(showText(level(playerLevel))))
                .append(text(" von ", YELLOW))
                .append(text(playerLevel.player().name(), GOLD, BOLD).hoverEvent(showText(player(playerLevel.player()))))
                .append(text(" wurden um ", YELLOW))
                .append(text(skillpoints, AQUA)).append(text(" Skillpunkt(e) auf ", YELLOW))
                .append(text(playerLevel.skillPoints(), AQUA))
                .append(text(" erhöht.", YELLOW));
    }

    public static TextComponent addExp(PlayerLevel playerLevel, int exp) {

        return text("Die ", YELLOW)
                .append(text("Erfahrungspunkte", AQUA, BOLD).hoverEvent(showText(level(playerLevel))))
                .append(text(" von ", YELLOW))
                .append(text(playerLevel.player().name(), GOLD, BOLD).hoverEvent(showText(player(playerLevel.player()))))
                .append(text(" wurden um ", YELLOW))
                .append(text(exp, AQUA)).append(text(" EXP auf ", YELLOW))
                .append(text(playerLevel.totalExp(), AQUA))
                .append(text(" erhöht.", YELLOW));
    }

    public static TextComponent addLevel(PlayerLevel playerLevel, int level) {

        return text("Das ", YELLOW)
                .append(text("Level", AQUA, BOLD).hoverEvent(showText(level(playerLevel))))
                .append(text(" von ", YELLOW))
                .append(text(playerLevel.player().name(), GOLD, BOLD).hoverEvent(showText(player(playerLevel.player()))))
                .append(text(" wurde um ", YELLOW))
                .append(text(level, AQUA)).append(text(" Level auf ", YELLOW))
                .append(text(playerLevel.level(), AQUA))
                .append(text(" erhöht.", YELLOW));
    }

    public static TextComponent level(PlayerLevel level) {

        return text("Level: ", YELLOW).append(text(level.level(), AQUA)).append(newline())
                .append(text("EXP: ", YELLOW)).append(text(level.totalExp(), AQUA)).append(newline())
                .append(text("Skillpunkte: ", YELLOW)).append(text(level.skillPoints(), AQUA));
    }

    public static TextComponent player(SkilledPlayer player) {

        Set<PlayerSkill> skills = player.skills();
        long unlockedSkills = skills.stream().filter(PlayerSkill::unlocked).count();
        long activeSkills = skills.stream().filter(PlayerSkill::active).count();
        return text()
                .append(text("--- [ ", DARK_AQUA))
                .append(text(player.name(), GOLD))
                .append(text(" ] ---", DARK_AQUA)).append(newline())
                .append(level(player.level())).append(newline())
                .append(text("Skills: ", YELLOW))
                .append(text(activeSkills, GREEN)).append(text("/", YELLOW)).append(text(unlockedSkills, DARK_GREEN))
                .build();
    }

    public static TextComponent skill(PlayerSkill skill) {

        return text("--- [ ", DARK_AQUA)
                .append(text(skill.name(), skillColor(skill), BOLD))
                .append(text(" (" + skill.alias() + ")", GRAY, ITALIC))
                .append(text(" ] ---", DARK_AQUA)).append(newline())
                .append(text(skill.description(), GRAY, ITALIC)).append(newline()).append(newline())
                .append(text("Vorraussetzungen:", YELLOW)).append(newline())
                .append(requirements(skill));
    }

    public static TextComponent requirements(PlayerSkill skill) {

        TextComponent.Builder text = text();
        for (Requirement requirement : skill.skill().requirements()) {
            text.append(text(" - ", YELLOW))
                    .append(text(requirement.name(), requirementColor(requirement, skill.player()), BOLD))
                    .hoverEvent(showText(requirement(requirement, skill.player())))
                    .append(newline());
        }
        return text.build();
    }

    public static TextComponent requirement(Requirement requirement, SkilledPlayer player) {

        return text("--- [ ", AQUA).append(text(requirement.name(), requirementColor(requirement, player)))
                .append(newline())
                .append(text(requirement.description(), GRAY, ITALIC));
    }

    public static TextColor requirementColor(Requirement requirement, SkilledPlayer player) {

        if (player == null) return YELLOW;
        return requirement.test(player).success() ? GREEN : RED;
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
