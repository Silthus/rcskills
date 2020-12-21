package de.raidcraft.skills;

import co.aikar.commands.CommandIssuer;
import de.raidcraft.economy.wrapper.Economy;
import de.raidcraft.skills.entities.ConfiguredSkill;
import de.raidcraft.skills.entities.Level;
import de.raidcraft.skills.entities.PlayerSkill;
import de.raidcraft.skills.entities.SkilledPlayer;
import lombok.AccessLevel;
import lombok.Setter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.feature.pagination.Pagination;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_RED;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

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

    public static void send(Object commandIssuer, Component message) {

        if (commandIssuer instanceof Player) {
            sendPlayer((Player) commandIssuer, message);
        } else if (commandIssuer instanceof ConsoleCommandSender) {
            sendConsole((ConsoleCommandSender) commandIssuer, message);
        } else if (commandIssuer instanceof RemoteConsoleCommandSender) {
            sendRemote((RemoteConsoleCommandSender) commandIssuer, message);
        } else if (commandIssuer instanceof CommandIssuer) {
            send((Object) ((CommandIssuer) commandIssuer).getIssuer(), message);
        }
    }

    public static void sendPlayer(Player player, Component message) {
        send(player.getUniqueId(), message);
    }

    public static void sendConsole(ConsoleCommandSender sender, Component message) {

        sender.sendMessage(PlainComponentSerializer.plain().serialize(message));
    }

    public static void sendRemote(RemoteConsoleCommandSender sender, Component message) {

        sender.sendMessage(PlainComponentSerializer.plain().serialize(message));
    }

    public static Component buySkill(SkilledPlayer player, PlayerSkill skill) {

        TextComponent.Builder builder = text().append(player(player))
                .append(text(" hat den Skill ", GREEN))
                .append(skill(skill, false))
                .append(text(" gekauft.", GREEN)).append(newline());

        int skillpoints = skill.configuredSkill().skillpoints();
        if (skillpoints > 0) {
            builder.append(text("  - Skillpunkte: ", YELLOW))
                    .append(text("-", DARK_RED, BOLD))
                    .append(text(skillpoints, RED)).append(newline());
        }

        double cost = skill.configuredSkill().money();
        if (cost > 0d) {
            String currencyNamePlural = Economy.get().currencyNamePlural();
            builder.append(text("  - " + currencyNamePlural + ": ", YELLOW))
                    .append(text("-", DARK_RED, BOLD))
                    .append(text(cost, RED)).append(newline());
        }

        return builder.build();
    }

    public static Component addSkill(SkilledPlayer player, PlayerSkill skill) {

        return text().append(player(player))
                .append(text(" hat den Skill ", GREEN))
                .append(skill(skill, false))
                .append(text(" erhalten.", GREEN)).build();
    }

    public static Component removeSkill(PlayerSkill playerSkill) {

        return text("Der Skill ", RED)
                .append(skill(playerSkill, true))
                .append(text(" wurde von ", RED))
                .append(player(playerSkill.player()))
                .append(text(" entfernt.", RED));
    }

    public static Component addSkillpoints(SkilledPlayer player, int skillpoints) {

        if (skillpoints == 0) return empty();

        return text("Die Skillpunkte von ", YELLOW)
                .append(player(player))
                .append(text(" wurden um ", YELLOW))
                .append(text(skillpoints, AQUA))
                .append(text(" Skillpunkt(e) auf ", YELLOW))
                .append(text(player.skillPoints(), AQUA))
                .append(text(" erhöht.", YELLOW));
    }

    public static Component addSkillSlots(SkilledPlayer player, int slots) {

        if (slots == 0) return empty();

        return text("Die Skill Slots von ", YELLOW)
                .append(player(player))
                .append(text(" wurden um ", YELLOW))
                .append(text(slots, AQUA))
                .append(text(" Slots auf ", YELLOW))
                .append(text(player.skillSlots().size(), AQUA))
                .append(text(" erhöht.", YELLOW));
    }


    public static Component setSkillpoints(SkilledPlayer player, int skillpoints) {

        return text("Die Skillpunkte von ", YELLOW)
                .append(player(player))
                .append(text(" wurden auf ", YELLOW))
                .append(text(skillpoints, AQUA))
                .append(text(" Skillpunkt(e) gesetzt.", YELLOW));
    }

    public static Component setSkillSlots(SkilledPlayer player, int skillslots) {

        return text("Die Skill Slots von ", YELLOW)
                .append(player(player))
                .append(text(" wurden auf ", YELLOW))
                .append(text(skillslots, AQUA))
                .append(text(" Slot(s) gesetzt.", YELLOW));
    }

    public static Component addExp(SkilledPlayer player, int exp) {

        if (exp == 0) return empty();

        return text("Die Erfahrungspunkte von ", YELLOW)
                .append(player(player))
                .append(text(" wurden um ", YELLOW))
                .append(text(exp + " EXP", AQUA)).append(text(" auf ", YELLOW))
                .append(text(player.level().getTotalExp() + " EXP", AQUA))
                .append(text(" erhöht.", YELLOW));
    }

    public static Component setExp(SkilledPlayer player, int exp) {

        return text("Die Erfahrungspunkte von ", YELLOW)
                .append(player(player))
                .append(text(" wurden auf ", YELLOW))
                .append(text(exp + " EXP", AQUA))
                .append(text(" gesetzt.", YELLOW));
    }

    public static Component addLevel(SkilledPlayer player, int level) {

        return text("Das Level von ", YELLOW)
                .append(player(player))
                .append(text(" wurde um ", YELLOW))
                .append(text(level, AQUA)).append(text(" Level auf ", YELLOW))
                .append(text(player.level().getLevel(), AQUA))
                .append(text(" erhöht.", YELLOW));
    }

    public static Component setLevel(SkilledPlayer player, int level) {

        return text("Das Level von ", YELLOW)
                .append(player(player))
                .append(text(" wurde auf ", YELLOW))
                .append(text("Level " + level, AQUA))
                .append(text(" gesetzt.", YELLOW));
    }

    public static BossBar levelProgressBar(int level, long exp, long expToNextLevel) {

        TextComponent title = text("Level " + level, GOLD, BOLD)
                .append(text("  -  ", DARK_AQUA))
                .append(text(exp, GREEN))
                .append(text("/", YELLOW))
                .append(text(expToNextLevel, AQUA))
                .append(text(" EXP", YELLOW));

        float progress = exp * 1.0f / expToNextLevel;
        if (progress > 1f) progress = 1f;
        return BossBar.bossBar(title, progress, BossBar.Color.BLUE, BossBar.Overlay.NOTCHED_20);
    }

    public static Title levelUpTitle(int level) {

        TextComponent headline = text("Level Aufstieg!", GOLD);
        TextComponent subline = text("Du hast ", YELLOW).append(text(" Level " + level, GREEN))
                .append(text(" erreicht.", YELLOW));
        return Title.title(headline, subline);
    }

    public static Component levelUpSelf(SkilledPlayer player, int level) {

        return text().append(text("Du", GOLD, BOLD).hoverEvent(playerInfo(player)))
                .append(text(" bist im Level aufgestiegen: ", GREEN))
                .append(text("Level " + level, AQUA))
                .append(text(" erreicht.", GREEN)).build();
    }

    public static Component levelDownSelf(SkilledPlayer player, int level) {

        return text().append(text("Du", GOLD, BOLD).hoverEvent(playerInfo(player)))
                .append(text(" bist im Level abgestiegen: ", RED))
                .append(text("Level " + level, AQUA)).build();
    }

    public static Component levelUp(SkilledPlayer player) {

        return text().append(player(player))
                .append(text(" ist im Level aufgestiegen: ", GREEN))
                .append(text("Level " + player.level().getLevel(), AQUA))
                .append(text(" erreicht.", GREEN)).build();
    }

    public static Component levelDown(SkilledPlayer player) {

        return text().append(player(player))
                .append(text(" ist im Level abgestiegen: ", RED))
                .append(text("Level " + player.level().getLevel(), AQUA)).build();
    }

    public static Component level(Level level) {

        return text("Level: ", YELLOW).append(text(level.getLevel(), AQUA)).append(newline())
                .append(text("EXP: ", YELLOW)).append(text(level.getTotalExp(), AQUA));
    }

    public static Component skillPoints(SkilledPlayer player) {

        return text("Skillpunkte: ", YELLOW).append(text(player.skillPoints(), AQUA));
    }

    public static Component skillSlots(SkilledPlayer player) {

        int slots = player.skillSlots().size();
        return text("Skill Slots: ", YELLOW)
                .append(text(slots - player.freeSkillSlots(), GREEN))
                .append(text("/", DARK_AQUA))
                .append(text(slots, RED));
    }

    public static Component player(SkilledPlayer player) {

        return text(player.name(), GOLD, BOLD)
                .hoverEvent(showText(playerInfo(player)));
    }

    public static Component playerInfo(SkilledPlayer player) {

        return text().append(text("--- [ ", DARK_AQUA))
                .append(text(player.name(), GOLD))
                .append(text(" ] ---", DARK_AQUA)).append(newline())
                .append(level(player.level())).append(newline())
                .append(skillPoints(player)).append(newline())
                .append(skillSlots(player)).append(newline())
                .append(text("aktive/freigeschaltete Skills: ", YELLOW))
                .append(text(player.activeSkills().size(), GREEN)).append(text("/", DARK_AQUA))
                .append(text(player.unlockedSkills().size(), AQUA))
                .build();
    }

    public static List<Component> skills(SkilledPlayer player, int page) {

        return skills(player, page, skill -> true);
    }

    public static List<Component> skills(SkilledPlayer player, int page, Predicate<ConfiguredSkill> predicate) {

        List<ConfiguredSkill> allSkills = ConfiguredSkill.find.all().stream()
                .filter(predicate)
                .sorted(Comparator.comparingInt(ConfiguredSkill::level))
                .collect(Collectors.toUnmodifiableList());

        TextComponent header = text("Skills von ", DARK_AQUA).append(player(player));
        Pagination<ConfiguredSkill> pagination = Pagination.builder()
                .width(Pagination.WIDTH - 6)
                .build(header, new Pagination.Renderer.RowRenderer<>() {
                    @Override
                    public @NonNull Collection<Component> renderRow(ConfiguredSkill value, int index) {

                        if (value == null) return Collections.singletonList(empty());
                        return Collections.singletonList(skill(value, player));
                    }
                }, p -> "/rcskills skills " + player.name() + " " + p);
        return pagination.render(allSkills, page);
    }

    public static Component skills(Collection<PlayerSkill> skills) {

        if (skills.isEmpty()) return empty();

        TextComponent.Builder builder = text();
        for (PlayerSkill skill : skills) {
            builder.append(skill(skill.configuredSkill(), skill.player()));
        }
        return builder.build();
    }

    public static Component skill(PlayerSkill skill, boolean showBuy) {

        return skill(skill.configuredSkill(), skill.player());
    }

    public static Component skill(ConfiguredSkill skill, SkilledPlayer player) {

        return skill(skill, player, true);
    }

    public static Component skill(ConfiguredSkill skill, SkilledPlayer player, boolean showBuy) {

        TextColor color = GRAY;
        if (player != null) {
            if (player.hasActiveSkill(skill)) {
                color = GREEN;
            } else {
                color = skill.test(player).success() ? AQUA : RED;
            }
        }

        TextComponent.Builder builder = text()
                .append(text("[", YELLOW))
                .append(text(skill.level(), AQUA))
                .append(text("] ", YELLOW))
                .append(text(skill.name(), color, BOLD).hoverEvent(skillInfo(skill, player)));

        if (showBuy && player != null && player.canBuy(skill)) {
            builder.append(text(" [$]", GREEN, ITALIC)
                    .hoverEvent(costs(PlayerSkill.getOrCreate(player, skill)).append(text("Klicken um den Skill zu kaufen.", GRAY, ITALIC)))
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/rcskills buy " + skill.id()))
            );
        }

        return builder.build();
    }

    public static Component skillInfo(ConfiguredSkill skill, SkilledPlayer player) {

        TextComponent.Builder builder = text().append(skillInfo(skill));
        if (player != null) {
            PlayerSkill playerSkill = PlayerSkill.getOrCreate(player, skill);
            builder.append(costs(playerSkill)).append(newline())
                    .append(requirements(playerSkill));
        }
        return builder.build();
    }

    public static Component skillInfo(ConfiguredSkill skill) {

        TextComponent.Builder builder = text().append(text("--- [ ", DARK_AQUA))
                .append(text(skill.name(), YELLOW, BOLD))
                .append(text(" (" + skill.alias() + ")", GRAY, ITALIC))
                .append(text(" ] ---", DARK_AQUA)).append(newline())
                .append(text("Level: ", YELLOW).append(text(skill.level(), AQUA))).append(newline());
        if (skill.money() > 0d) {
            builder.append(text("Kosten: ", YELLOW))
                    .append(text(Economy.get().format(skill.money()), AQUA))
                    .append(newline());
        }
        if (skill.skillpoints() > 0) {
            builder.append(text("Skillpunkte: ", YELLOW))
                    .append(text(skill.skillpoints(), AQUA))
                    .append(newline());
        }
        return builder
                .append(text("Beschreibung: ", YELLOW))
                .append(text(skill.description(), GRAY, ITALIC)).append(newline())
                .build();
    }

    public static Component allRequirements(PlayerSkill skill) {

        return text().append(costs(skill)).append(requirements(skill)).build();
    }

    public static Component requirements(PlayerSkill skill) {

        TextComponent.Builder builder = text();
        List<Requirement> requirements = skill.configuredSkill().requirements().stream()
                .filter(Requirement::visible)
                .collect(Collectors.toUnmodifiableList());

        if (!requirements.isEmpty()) {
            builder.append(text("Vorraussetzungen: ", YELLOW)).append(newline());
            for (Requirement requirement : requirements) {
                builder.append(text(" - ", YELLOW))
                        .append(text(requirement.name(), requirementColor(requirement, skill.player()), BOLD)
                        .hoverEvent(showText(requirement(requirement, skill.player()))))
                        .append(newline());
            }
        }

        return builder.build();
    }

    public static Component costs(PlayerSkill skill) {

        TextComponent.Builder builder = text();
        List<Requirement> costs = skill.configuredSkill().costRequirements();

        if (!costs.isEmpty()) {
            builder.append(text("Kosten:", YELLOW)).append(newline());
            for (Requirement cost : costs) {
                builder.append(text(" - ", YELLOW))
                        .append(text(cost.name(), requirementColor(cost, skill.player()), BOLD)
                                .hoverEvent(showText(requirement(cost, skill.player()))))
                        .append(newline());
            }
        }

        return builder.build();
    }

    public static Component requirement(Requirement requirement, SkilledPlayer player) {

        return text().append(text("--- [ ", DARK_AQUA))
                .append(text(requirement.name(), requirementColor(requirement, player)))
                .append(text(" ] ---", DARK_AQUA))
                .append(newline())
                .append(text(requirement.description(), GRAY, ITALIC))
                .build();
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

    public static TextReplacementConfig replaceLevel(Level level) {

        return TextReplacementConfig.builder()
                .matchLiteral("{level}").replacement(level(level)).build();
    }

    public static TextReplacementConfig replaceSkill(PlayerSkill skill) {

        return TextReplacementConfig.builder()
                .matchLiteral("{skill}").replacement(skill(skill, true))
                .matchLiteral("{skill_name}").replacement(skill.name())
                .matchLiteral("{skill_alias}").replacement(skill.alias())
                .build();
    }

    public static TextColor skillColor(PlayerSkill skill) {

        switch (skill.status()) {
            case ACTIVE:
                return GREEN;
            case UNLOCKED:
                return GRAY;
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
            instance = this;
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
