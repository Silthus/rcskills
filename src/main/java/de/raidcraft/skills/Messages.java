package de.raidcraft.skills;

import co.aikar.commands.CommandIssuer;
import de.raidcraft.economy.wrapper.Economy;
import de.raidcraft.skills.commands.PlayerCommands;
import de.raidcraft.skills.entities.*;
import lombok.AccessLevel;
import lombok.Setter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.feature.pagination.Pagination;
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
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.event.ClickEvent.*;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
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

        if (commandIssuer instanceof SkilledPlayer) {
            send(((SkilledPlayer) commandIssuer).id(), message);
        } else if (commandIssuer instanceof Player) {
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
        if (progress < 0f) progress = 0f;
        return BossBar.bossBar(title, progress, BossBar.Color.BLUE, BossBar.Overlay.NOTCHED_20);
    }

    public static Title levelUpTitle(int level) {

        TextComponent headline = text("Level Aufstieg!", GOLD);
        TextComponent subline = text("Du hast ", YELLOW).append(text(" Level " + level, GREEN))
                .append(text(" erreicht.", YELLOW));
        return Title.title(headline, subline);
    }

    public static Component addSkillPointsSelf(SkilledPlayer player, int skillpoints) {

        if (skillpoints == 0) return empty();

        return text().append(text("Du", GOLD, BOLD).hoverEvent(showText(playerInfo(player))))
                .append(text(" hast ", GREEN))
                .append(text(skillpoints + " Skillpunkt(e)", AQUA))
                .append(text(" erhalten!", GREEN))
                .build();
    }

    public static Component addSkillSlotsSelf(SkilledPlayer player, int slots) {

        if (slots == 0) return empty();

        return text().append(text("Du", GOLD, BOLD).hoverEvent(showText(playerInfo(player))))
                .append(text(" hast ", GREEN))
                .append(text(slots + " Skill Slot(s)", AQUA))
                .append(text(" erhalten!", GREEN))
                .build();
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

    public static Component level(SkilledPlayer player) {

        Level level = player.level();
        LevelManager levelManager = SkillsPlugin.instance().getLevelManager();
        int expToNext = levelManager.calculateExpToNextLevel(player);
        long exp;
        if (level.getLevel() == 1) {
            exp = level.getTotalExp();
        } else {
            exp = level.getTotalExp() - levelManager.getTotalExpForLevel(level.getLevel());
        }

        return text("Level: ", YELLOW).append(text(level.getLevel(), AQUA)).append(newline())
                .append(text("EXP: ", YELLOW)).append(text(exp, GREEN)).append(text("/", YELLOW))
                .append(text(expToNext, AQUA)).append(newline())
                .append(text("Gesamt EXP: ", YELLOW)).append(text(level.getTotalExp(), AQUA));
    }

    public static Component skillPoints(SkilledPlayer player) {

        return text("Skillpunkte: ", YELLOW).append(text(player.skillPoints(), AQUA));
    }

    public static Component skillSlots(SkilledPlayer player) {

        List<SkillSlot> slots = player.skillSlots().stream()
                .sorted()
                .collect(Collectors.toUnmodifiableList());
        int freeSkillSlots = player.freeSkillSlots();

        Optional<Integer> nextSlot = SkillsPlugin.instance().getPluginConfig()
                .getLevelUpConfig()
                .getNextLevelUpSlot(player.level().getLevel());

        SlotManager slotManager = SkillsPlugin.instance().getSlotManager();
        int slotCount = player.slotCount();
        double firstSlot = slotManager.calculateSlotCost(player, slotCount + 1);
        double secondSlot = slotManager.calculateSlotCost(player, slotCount + 2);
        double resetCost = slotManager.calculateSlotResetCost(player);

        TextComponent.Builder builder = text()
                .append(text("Slots ", YELLOW))
                .append(text("(", GRAY))
                .append(text(freeSkillSlots, freeSkillSlots > 0 ? GREEN : RED))
                .append(text("/", DARK_AQUA))
                .append(text(slots.size(), AQUA))
                .append(text(")", GRAY))
                .append(text(" [?]", GRAY).hoverEvent(showText(text()
                        .append(text("Nächster Slot: ", YELLOW))
                        .append(nextSlot.map(integer -> text("auf Level " + integer, GREEN))
                                .orElse(text("N/A", GRAY))).append(newline())
                        .append(text("Slot Kosten: ", YELLOW)).append(newline())
                        .append(text(" - ", YELLOW)).append(text((slotCount + 1) + ". Slot: ", DARK_AQUA))
                        .append(text(Economy.get().format(firstSlot), Economy.get().has(player.offlinePlayer(), firstSlot) ? GREEN : RED)).append(newline())
                        .append(text(" - ", YELLOW)).append(text((slotCount + 2) + ". Slot: ", DARK_AQUA))
                        .append(text(Economy.get().format(secondSlot), Economy.get().has(player.offlinePlayer(), secondSlot) ? GREEN : RED))
                        .append(newline()).append(newline())
                        .append(text("Du kannst deine Skill Slots mit ", GRAY)).append(text("/rcs reset", GOLD))
                        .append(text(" für ", GRAY)).append(text(Economy.get().format(resetCost), Economy.get().has(player.offlinePlayer(), resetCost) ? GREEN : RED))
                        .append(text(" zurücksetzen.", GRAY)).append(newline())
                        .append(text("Die Kosten für das Zurücksetzen steigen jedes Mal weiter an.", GRAY))
                        .append(newline()).append(newline())
                        .append(text("Tipp: ", GREEN)).append(text("Du erhältst neue Skill Slots beim Level Aufstieg, " +
                                "durch Events und Achievements.", GRAY, ITALIC))
                )))
                .append(text(": ", YELLOW));

        if (slots.isEmpty()) {
            builder.append(text("N/A", GRAY));
        }

        for (int i = 0; i < slots.size(); i++) {
            builder.append(skillSlot(slots.get(i)));
            if (i != slots.size() - 1) {
                builder.append(text(" | ", YELLOW));
            }
        }

        return builder.build();
    }

    public static Component skillSlot(SkillSlot slot) {

        TextComponent.Builder builder = text();
        SkilledPlayer player = slot.player();
        if (slot.status() == SkillSlot.Status.IN_USE) {
            builder.append(text("[", DARK_AQUA))
                    .append(text("*", DARK_AQUA)
                            .hoverEvent(showText(slot.skill().map(skill -> skillInfo(skill.configuredSkill(), player)).orElse(empty()))))
                    .append(text("]", DARK_AQUA));
        } else if (slot.status() == SkillSlot.Status.FREE) {
            return builder.append(text("[", DARK_AQUA)).append(text("O", GREEN)).append(text("]", DARK_AQUA))
                    .build().hoverEvent(showText(text("Aktiviere einen Skill um den Skill Slot zu belegen.", GRAY)))
                    .clickEvent(suggestCommand(PlayerCommands.activateSkill(null)));
        } else if (slot.status() == SkillSlot.Status.ELIGIBLE) {
            double cost = SkillsPlugin.instance().getSlotManager().calculateSlotCost(player);
            return builder.append(text("[", RED))
                    .append(text("$", Economy.get().has(player.offlinePlayer(), cost) ? GREEN : DARK_RED))
                    .append(text("]", RED)).build()
                    .hoverEvent(showText(text("Du kannst diesen Skill Slot für ", GRAY)
                            .append(text(Economy.get().format(cost), AQUA))
                            .append(text(" kaufen.", GRAY)).append(newline()).append(newline())
                            .append(text("Klicke um den Slot zu kaufen."))
                    )).clickEvent(runCommand(PlayerCommands.buySkillSlot()));
        }

        slot.skill().ifPresent(skill -> builder.build().hoverEvent(showText(skillInfo(skill.configuredSkill(), player))));

        return builder.build();
    }

    public static Component activeSkills(SkilledPlayer player) {

        return text().append(text("aktive Skills: ", YELLOW))
                .append(text(player.activeSkills().size(), GREEN)).append(text("/", DARK_AQUA))
                .append(text(player.unlockedSkills().size(), AQUA))
                .build();
    }

    public static Component player(SkilledPlayer player) {

        return text(player.name(), GOLD, BOLD)
                .hoverEvent(showText(playerInfo(player)));
    }

    public static Component playerInfo(SkilledPlayer player) {

        return text().append(text("--- [ ", DARK_AQUA))
                .append(text(player.name(), GOLD))
                .append(text(" ] ---", DARK_AQUA)).append(newline())
                .append(level(player)).append(newline())
                .append(skillPoints(player)).append(newline())
                .append(skillSlots(player)).append(newline())
                .append(activeSkills(player))
                .build();
    }

    public static List<Component> skills(SkilledPlayer player, int page) {

        return skills(player, page, skill -> true);
    }

    public static List<Component> skills(SkilledPlayer player, int page, Predicate<ConfiguredSkill> predicate) {

        List<ConfiguredSkill> allSkills = ConfiguredSkill.find.all().stream()
                .filter(skill -> !skill.hidden())
                .filter(skill -> !skill.isChild())
                .filter(predicate)
                .sorted(Comparator.comparingInt(ConfiguredSkill::level))
                .collect(Collectors.toUnmodifiableList());

        TextComponent header = text("Skills von ", DARK_AQUA).append(player(player));
        return skills(header, player, allSkills, page);
    }

    public static List<Component> skills(Component header, SkilledPlayer player, Collection<ConfiguredSkill> skills, int page) {

        Pagination<ConfiguredSkill> pagination = Pagination.builder()
                .width(Pagination.WIDTH - 6)
                .resultsPerPage(6)
                .build(header, new Pagination.Renderer.RowRenderer<>() {
                    @Override
                    public @NonNull Collection<Component> renderRow(ConfiguredSkill value, int index) {

                        if (value == null) return Collections.singletonList(empty());
                        return Collections.singletonList(skill(value, player));
                    }
                }, p -> "/rcskills info " + player.name() + " " + p);
        return pagination.render(skills, page);
    }

    public static Component skills(List<PlayerSkill> skills) {

        if (skills.isEmpty()) return empty();

        TextComponent.Builder builder = text();
        for (int i = 0; i < skills.size(); i++) {
            builder.append(text("  - ", YELLOW)).append(skill(skills.get(i).configuredSkill(), skills.get(i).player(), true, false));
            if (i != skills.size() - 1) {
                builder.append(newline());
            }
        }

        return builder.build();
    }

    public static Component skill(PlayerSkill skill, boolean showDetails) {

        return skill(skill.configuredSkill(), skill.player(), showDetails, showDetails);
    }

    public static Component skill(ConfiguredSkill skill, SkilledPlayer player) {

        return skill(skill, player, true, true);
    }

    public static Component skill(ConfiguredSkill skill, SkilledPlayer player, boolean showDetails, boolean showChildren) {

        TextColor color = GRAY;
        if (skill.disabled()) {
            color = DARK_GRAY;
        } else if (player != null) {
            if (player.hasActiveSkill(skill)) {
                color = GREEN;
            } else if (player.hasSkill(skill)) {
                color = DARK_AQUA;
            } else {
                color = player.canBuy(skill) ? GRAY : RED;
            }
        }

        TextColor levelColor = AQUA;
        if (player != null) {
            levelColor = player.level().getLevel() >= skill.level() ? GREEN : RED;
        }

        TextComponent.Builder builder = text();

        if (showDetails) {
            builder.append(text("[", YELLOW))
                    .append(text(skill.level(), levelColor))
                    .append(text("] ", YELLOW));
        }

        builder.append(text(skill.name(), color, BOLD));

        if (skill.disabled()) {
            return builder.hoverEvent(showText(text("Der Skill ist deaktiviert.", RED))).build();
        }

        builder.hoverEvent(skillInfo(skill, player));

        if (player != null && showDetails) {
            PlayerSkill playerSkill = PlayerSkill.getOrCreate(player, skill);
            if (player.canBuy(skill)) {
                builder.append(text(" | ", YELLOW)).append(text(" [$] ", GREEN)
                        .hoverEvent(costs(playerSkill).append(text("Klicken um den Skill zu kaufen.", GRAY, ITALIC)))
                        .clickEvent(clickEvent(Action.RUN_COMMAND, PlayerCommands.buySkill(player, skill)))
                );
            } else if (playerSkill.active() && !playerSkill.isChild()) {
                builder.append(text(" | ", YELLOW)).append(text("aktiv", AQUA).hoverEvent(HoverEvent.showText(
                        text("Der Skill ist aktiv.", GRAY).append(newline())
                                .append(text("Gebe ", GRAY))
                                .append(text(PlayerCommands.reset(player), GOLD, ITALIC))
                                .append(text(" ein um alle deine Skills zurückzusetzen.", GRAY)).append(newline())
                            .append(text("Klicke um ", GRAY, ITALIC).append(text(PlayerCommands.reset(player), GOLD, ITALIC)))
                            .append(text(" auszuführen.", GRAY, ITALIC))
                        )).clickEvent(suggestCommand(PlayerCommands.reset(player)))
                );
            } else if (playerSkill.unlocked() && !playerSkill.isChild()) {
                if (playerSkill.canActivate()) {
                    builder.append(text(" | ", YELLOW)).append(text("aktivieren", GREEN).hoverEvent(HoverEvent.showText(
                            text("Du besitzt den Skill, er ist aber nicht aktiv.", GRAY).append(newline())
                                    .append(text("Klicke um den Skill zu aktivieren und einem Slot zuzuweisen.", GRAY)).append(newline())
                                    .append(skillSlots(player))))
                            .clickEvent(runCommand(PlayerCommands.activateSkill(playerSkill)))
                    );
                } else {
                    Optional<Map.Entry<Integer, SkillPluginConfig.LevelUp>> nextLevelUp = SkillsPlugin.instance().getPluginConfig()
                            .getLevelUpConfig()
                            .getNextLevelUp(player.level().getLevel());

                    TextComponent hover = text("Du besitzt den Skill, kannst ihn aber nicht aktivieren, da du keinen freien Skill Slot hast.", GRAY).append(newline());
                    if (nextLevelUp.isPresent() && nextLevelUp.get().getValue().getSlots() > 0) {
                        hover.append(text("Mit ", GRAY))
                                .append(text("Level " + nextLevelUp.get().getKey(), DARK_AQUA))
                                .append(text(" erhältst du ", GRAY))
                                .append(text(nextLevelUp.get().getValue().getSlots() + " Skill Slot(s)", GREEN))
                                .append(text(".", GRAY)).append(newline());
                    }
                    hover.append(text("Mit Events und Achievements kannst du jederzeit weitere Skill Slots erhalten.", GRAY, ITALIC));

                    builder.append(text(" | ", YELLOW)).append(text("aktivieren", GRAY).hoverEvent(showText(hover)));
                }
            }
            if (skill.executable() && playerSkill.active() && !playerSkill.isChild()) {
                builder.append(text(" | ", YELLOW));
                List<ItemBinding> bindings = player.bindings().get(playerSkill);
                TextComponent.Builder hover = text().append(text("Klicke um den Skill auf das Item in deiner Hand zu binden.", GRAY));
                if (!bindings.isEmpty()) {
                    hover.append(newline()).append(text("Bestehende Item Bindings: ", YELLOW)).append(newline());
                    for (ItemBinding binding : bindings) {
                        hover.append(text(" - ", YELLOW))
                                .append(text(binding.material().getKey().getKey(), DARK_AQUA))
                                .append(text(" (" + binding.action().friendlyName() + ")")).append(newline());
                    }
                }
                builder.append(text("bind", GOLD)
                        .hoverEvent(hover.build())
                        .clickEvent(suggestCommand(PlayerCommands.bindSkill(playerSkill)))
                );
            }
        }

        if (skill.isParent() && showChildren) {

            List<ConfiguredSkill> childSkills = skill.children().stream()
                    .filter(ConfiguredSkill::visible)
                    .collect(Collectors.toList());
            if (childSkills.size() > 0) {
                for (ConfiguredSkill child : childSkills) {
                    builder.append(newline());
                    ConfiguredSkill tmp = child.parent();
                    while (tmp.isChild()) {
                        builder.append(text(" "));
                        tmp = tmp.parent();
                    }
                    builder.append(text(" \u2937 ", YELLOW, BOLD)) //⤷
                            .append(skill(child, player, showDetails, showChildren));
                }
            }

        }

        return builder.build();
    }

    public static Component skillInfo(ConfiguredSkill skill, SkilledPlayer player) {

        TextComponent.Builder builder = text().append(skillInfo(skill));
        if (player != null) {
            PlayerSkill playerSkill = PlayerSkill.getOrCreate(player, skill);
            builder.append(text("Status: ", YELLOW)).append(text(playerSkill.configuredSkill().disabled() ? "deaktiviert" : playerSkill.status().localized(), AQUA)).append(newline())
                    .append(costs(playerSkill)).append(newline())
                    .append(requirements(playerSkill));
            if (skill.executable()) {
                builder.append(newline())
                        .append(text("Tipp: ", GREEN).append(text("Du kannst den Skill mit "))
                                .append(text("/bind " + skill.alias(), GOLD, ITALIC))
                                .append(text(" auf Gegenstände binden um sie mit einem Rechts oder Linksklick auszuführen.", GRAY, ITALIC)));
            }
        }
        return builder.build();
    }

    public static Component skillInfo(ConfiguredSkill skill) {

        TextComponent.Builder builder = text().append(text(skill.name(), GOLD))
                .append(text(" (" + skill.alias() + ")", GRAY, ITALIC)).append(newline())
                .append(text("Level: ", YELLOW).append(text(skill.level(), AQUA))).append(newline())
                .append(text("Typ: ", YELLOW));
        if (skill.executable()) {
            builder.append(text("AKTIV", GREEN, BOLD)).append(newline());
        } else {
            builder.append(text("PASSIV", DARK_AQUA)).append(newline());
        }

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

    public static Component resultOf(ExecutionResult executionResult) {

        TextComponent.Builder builder = text().append(text("Der Skill ", YELLOW)
                .append(skill(executionResult.context().source().playerSkill(), false)));

        switch (executionResult.status()) {
            case SUCCESS:
                return builder.append(text(" wurde ", YELLOW)
                        .append(text("erfolgreich", GREEN))
                        .append(text(" ausgeführt.", YELLOW)))
                        .build();
            case COOLDOWN:
                return builder.append(text(" hat noch einen Cooldown von ", YELLOW)
                        .append(text(executionResult.formattedCooldown(), AQUA))
                        .append(text(".", YELLOW)))
                        .build();
            case DELAYED:
                return builder.append(text(" wird in ", YELLOW)
                        .append(text(executionResult.formattedDelay(), AQUA))
                        .append(text(" ausgeführt.", YELLOW)))
                        .build();
            case EXCEPTION:
            case FAILURE:
                return builder.append(text(" konnte nicht ausgeführt werden.", YELLOW).append(newline())
                        .append(text("Bei der Ausführung des Skills ist ein Fehler aufgetreten: ", RED))
                        .append(text(String.join(",", executionResult.errors()), RED)))
                        .build();
        }

        return builder.append(text(" wurde ausgeführt.", YELLOW)).build();
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
