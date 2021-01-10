package de.raidcraft.skills;

import co.aikar.commands.CommandIssuer;
import de.raidcraft.economy.wrapper.Economy;
import de.raidcraft.skills.commands.PlayerCommands;
import de.raidcraft.skills.entities.*;
import de.raidcraft.skills.util.TimeUtil;
import lombok.AccessLevel;
import lombok.Setter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
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

import static de.raidcraft.skills.Messages.Colors.*;
import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.event.ClickEvent.*;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

public final class Messages {

    public static final class Colors {

        public static final TextColor BASE = YELLOW;
        public static final TextColor INACTIVE = GRAY;
        public static final TextColor DISABLED = DARK_GRAY;
        public static final TextColor ACCENT = GOLD;
        public static final TextColor DARK_ACCENT = DARK_AQUA;
        public static final TextColor HIGHLIGHT = AQUA;
        public static final TextColor DARK_HIGHLIGHT = DARK_AQUA;
        public static final TextColor ACTIVE = GREEN;
        public static final TextColor ENABLED = DARK_GREEN;
        public static final TextColor UNLOCKED = GOLD;
        public static final TextColor ERROR = RED;
        public static final TextColor ERROR_ACCENT = DARK_RED;
        public static final TextColor SUCCESS = GREEN;
        public static final TextColor WARNING = GOLD;
        public static final TextColor NOTE = GRAY;
        public static final TextColor TEXT = BASE;
    }

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
                .append(text(" hat den Skill ", SUCCESS))
                .append(skill(skill, false))
                .append(text(" gekauft.", SUCCESS)).append(newline());

        int skillpoints = skill.configuredSkill().skillpoints();
        if (skillpoints > 0) {
            builder.append(text("  - Skillpunkte: ", TEXT))
                    .append(text("-", ERROR_ACCENT, BOLD))
                    .append(text(skillpoints, ERROR)).append(newline());
        }

        double cost = skill.configuredSkill().money();
        if (cost > 0d) {
            String currencyNamePlural = Economy.get().currencyNamePlural();
            builder.append(text("  - " + currencyNamePlural + ": ", TEXT))
                    .append(text("-", ERROR_ACCENT, BOLD))
                    .append(text(cost, ERROR)).append(newline());
        }

        return builder.build();
    }

    public static Component addSkill(SkilledPlayer player, PlayerSkill skill) {

        return text().append(player(player))
                .append(text(" hat den Skill ", SUCCESS))
                .append(skill(skill, false))
                .append(text(" erhalten.", SUCCESS)).build();
    }

    public static Component removeSkill(PlayerSkill playerSkill) {

        return text("Der Skill ", ERROR)
                .append(skill(playerSkill, false))
                .append(text(" wurde von ", ERROR))
                .append(player(playerSkill.player()))
                .append(text(" entfernt.", ERROR));
    }

    public static Component addSkillpoints(SkilledPlayer player, int skillpoints) {

        if (skillpoints == 0) return empty();

        return text("Die Skillpunkte von ", SUCCESS)
                .append(player(player))
                .append(text(" wurden um ", SUCCESS))
                .append(text(skillpoints, HIGHLIGHT))
                .append(text(" Skillpunkt(e) auf ", SUCCESS))
                .append(text(player.skillPoints(), HIGHLIGHT))
                .append(text(" erhöht.", SUCCESS));
    }

    public static Component addSkillSlots(SkilledPlayer player, int slots) {

        if (slots == 0) return empty();

        return text("Die Skill Slots von ", SUCCESS)
                .append(player(player))
                .append(text(" wurden um ", SUCCESS))
                .append(text(slots, HIGHLIGHT))
                .append(text(" Slots auf ", SUCCESS))
                .append(text(player.skillSlots().size(), HIGHLIGHT))
                .append(text(" erhöht.", SUCCESS));
    }


    public static Component setSkillpoints(SkilledPlayer player, int skillpoints) {

        return text("Die Skillpunkte von ", TEXT)
                .append(player(player))
                .append(text(" wurden auf ", TEXT))
                .append(text(skillpoints, HIGHLIGHT))
                .append(text(" Skillpunkt(e) gesetzt.", TEXT));
    }

    public static Component setSkillSlots(SkilledPlayer player, int skillslots) {

        return text("Die Skill Slots von ", TEXT)
                .append(player(player))
                .append(text(" wurden auf ", TEXT))
                .append(text(skillslots, HIGHLIGHT))
                .append(text(" Slot(s) gesetzt.", TEXT));
    }

    public static Component addExp(SkilledPlayer player, int exp) {

        if (exp == 0) return empty();

        return text("Die Erfahrungspunkte von ", SUCCESS)
                .append(player(player))
                .append(text(" wurden um ", SUCCESS))
                .append(text(exp + " EXP", HIGHLIGHT))
                .append(text(" auf ", SUCCESS))
                .append(text(player.level().getTotalExp() + " EXP", HIGHLIGHT))
                .append(text(" erhöht.", SUCCESS));
    }

    public static Component setExp(SkilledPlayer player, int exp) {

        return text("Die Erfahrungspunkte von ", TEXT)
                .append(player(player))
                .append(text(" wurden auf ", TEXT))
                .append(text(exp + " EXP", HIGHLIGHT))
                .append(text(" gesetzt.", TEXT));
    }

    public static Component addLevel(SkilledPlayer player, int level) {

        return text("Das Level von ", SUCCESS)
                .append(player(player))
                .append(text(" wurde um ", SUCCESS))
                .append(text(level + " Level", HIGHLIGHT))
                .append(text(" auf ", SUCCESS))
                .append(text("Level " + player.level().getLevel(), HIGHLIGHT))
                .append(text(" erhöht.", SUCCESS));
    }

    public static Component setLevel(SkilledPlayer player, int level) {

        return text("Das Level von ", TEXT)
                .append(player(player))
                .append(text(" wurde auf ", TEXT))
                .append(text("Level " + level, HIGHLIGHT))
                .append(text(" gesetzt.", TEXT));
    }

    public static BossBar levelProgressBar(int level, long exp, long expToNextLevel) {

        TextComponent title = text("Level " + level, ACCENT, BOLD)
                .append(text("  -  ", DARK_ACCENT))
                .append(text(exp, HIGHLIGHT))
                .append(text("/", TEXT))
                .append(text(expToNextLevel, HIGHLIGHT))
                .append(text(" EXP", TEXT));

        float progress = exp * 1.0f / expToNextLevel;
        if (progress > 1f) progress = 1f;
        if (progress < 0f) progress = 0f;
        return BossBar.bossBar(title, progress, BossBar.Color.GREEN, BossBar.Overlay.NOTCHED_20);
    }

    public static Title levelUpTitle(int level) {

        TextComponent headline = text("Level Aufstieg!", ACCENT);
        TextComponent subline = text("Du hast ", SUCCESS)
                .append(text(" Level " + level, HIGHLIGHT))
                .append(text(" erreicht.", SUCCESS));
        return Title.title(headline, subline);
    }

    public static Component addExpSelf(SkilledPlayer player, int exp) {

        if (exp == 0) return empty();

        return text().append(text("Du", ACCENT, BOLD)
                .hoverEvent(showText(playerInfo(player))))
                .append(text(" hast ", SUCCESS))
                .append(text(exp + " EXP", HIGHLIGHT))
                .append(text(" erhalten!", SUCCESS))
                .build();
    }

    public static Component addLevelSelf(SkilledPlayer player, int level) {

        if (level == 0) return empty();

        return text().append(text("Du", ACCENT, BOLD).hoverEvent(showText(playerInfo(player))))
                .append(text(" hast ", SUCCESS))
                .append(text(level + " Level", HIGHLIGHT))
                .append(text(" erhalten!", SUCCESS))
                .build();
    }

    public static Component addSkillPointsSelf(SkilledPlayer player, int skillpoints) {

        if (skillpoints == 0) return empty();

        return text().append(text("Du", ACCENT, BOLD).hoverEvent(showText(playerInfo(player))))
                .append(text(" hast ", SUCCESS))
                .append(text(skillpoints + " Skillpunkt(e)", HIGHLIGHT))
                .append(text(" erhalten!", SUCCESS))
                .build();
    }

    public static Component addSkillSlotsSelf(SkilledPlayer player, int slots) {

        if (slots == 0) return empty();

        return text().append(text("Du", ACCENT, BOLD).hoverEvent(showText(playerInfo(player))))
                .append(text(" hast ", SUCCESS))
                .append(text(slots + " Skill Slot(s)", HIGHLIGHT))
                .append(text(" erhalten!", SUCCESS))
                .build();
    }

    public static Component addFreeResets(SkilledPlayer player, int freeResets) {

        if (freeResets < 1) return empty();

        return text().append(text(player.name(), ACCENT, BOLD)
                .hoverEvent(showText(playerInfo(player))))
                .append(text(" hat ", SUCCESS))
                .append(text(freeResets + " kostenlose(n) Reset(s)", HIGHLIGHT))
                .append(text(" für seine Skill Slots erhalten!", SUCCESS))
                .build();
    }

    public static Component addFreeResetsSelf(SkilledPlayer player, int freeResets) {

        if (freeResets < 1) return empty();

        return text().append(text("Du", ACCENT, BOLD).hoverEvent(showText(playerInfo(player))))
                .append(text(" hast ", SUCCESS))
                .append(text(freeResets + " kostenlose(n) Reset(s)", HIGHLIGHT))
                .append(text(" für deine Skill Slots erhalten!", SUCCESS))
                .build();
    }

    public static Component levelUpSelf(SkilledPlayer player, int level) {

        return text().append(text("Du", ACCENT, BOLD).hoverEvent(playerInfo(player)))
                .append(text(" bist im Level aufgestiegen: ", SUCCESS))
                .append(text("Level " + level, HIGHLIGHT))
                .append(text(" erreicht.", SUCCESS)).build();
    }

    public static Component levelDownSelf(SkilledPlayer player, int level) {

        return text().append(text("Du", ACCENT, BOLD).hoverEvent(playerInfo(player)))
                .append(text(" bist im Level abgestiegen: ", ERROR))
                .append(text("Level " + level, HIGHLIGHT)).build();
    }

    public static Component levelUp(SkilledPlayer player) {

        return text().append(player(player))
                .append(text(" ist im Level aufgestiegen: ", SUCCESS))
                .append(text("Level " + player.level().getLevel(), HIGHLIGHT))
                .append(text(" erreicht.", SUCCESS)).build();
    }

    public static Component levelDown(SkilledPlayer player) {

        return text().append(player(player))
                .append(text(" ist im Level abgestiegen: ", ERROR))
                .append(text("Level " + player.level().getLevel(), HIGHLIGHT)).build();
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

        return text("Level: ", TEXT)
                .append(text(level.getLevel(), HIGHLIGHT)).append(newline())
                .append(text("EXP: ", TEXT)).append(text(exp, HIGHLIGHT))
                .append(text("/", TEXT))
                .append(text(expToNext, DARK_HIGHLIGHT)).append(newline())
                .append(text("Gesamt EXP: ", TEXT)).append(text(level.getTotalExp(), HIGHLIGHT));
    }

    public static Component skillPoints(SkilledPlayer player) {

        return text("Skillpunkte: ", TEXT)
                .append(text(player.skillPoints(), HIGHLIGHT));
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
                .append(text("Slots ", TEXT))
                .append(text("(", NOTE))
                .append(text(freeSkillSlots, freeSkillSlots > 0 ? SUCCESS : ERROR))
                .append(text("/", DARK_ACCENT))
                .append(text(slots.size(), ACCENT))
                .append(text(")", NOTE))
                .append(text(" [?]", NOTE).hoverEvent(showText(text()
                        .append(text("Nächster Slot: ", TEXT))
                        .append(nextSlot.map(integer -> text("auf Level " + integer, SUCCESS))
                                .orElse(text("N/A", NOTE))).append(newline())
                        .append(text("Slot Kosten: ", TEXT)).append(newline())
                        .append(text(" - ", TEXT)).append(text((slotCount + 1) + ". Slot: ", DARK_ACCENT))
                        .append(text(Economy.get().format(firstSlot), Economy.get().has(player.offlinePlayer(), firstSlot) ? SUCCESS : ERROR))
                        .append(newline())
                        .append(text(" - ", TEXT)).append(text((slotCount + 2) + ". Slot: ", DARK_ACCENT))
                        .append(text(Economy.get().format(secondSlot), Economy.get().has(player.offlinePlayer(), secondSlot) ? SUCCESS : ERROR))
                        .append(newline()).append(newline())
                        .append(text("Du kannst deine Skill Slots mit ", NOTE)).append(text("/rcs reset", ACCENT))
                        .append(text(" zurücksetzen.", NOTE)).append(newline())
                        .append(text("Du hast noch ", NOTE))
                        .append(text(player.freeResets(), player.freeResets() > 0 ? SUCCESS : ERROR))
                        .append(text(" kostenlose(n) Reset(s).", player.freeResets() > 0 ? SUCCESS : ERROR))
                        .append(newline())
                        .append(text("Der nächste nicht kostenlose Reset kostet dich: ", NOTE))
                        .append(text(Economy.get().format(resetCost), Economy.get().has(player.offlinePlayer(), resetCost) ? SUCCESS : ERROR)).append(newline())
                        .append(text("Die Kosten für das Zurücksetzen steigen jedes Mal weiter an.", NOTE))
                        .append(newline()).append(newline())
                        .append(text("Tipp: ", HIGHLIGHT)).append(text("Du erhältst neue Skill Slots beim Level Aufstieg, " +
                                "durch Events und Achievements.", NOTE, ITALIC))
                )))
                .append(text(": ", TEXT));

        if (slots.isEmpty()) {
            builder.append(text("N/A", NOTE));
        }

        for (int i = 0; i < slots.size(); i++) {
            builder.append(skillSlot(slots.get(i)));
            if (i != slots.size() - 1) {
                builder.append(text(" | ", DARK_ACCENT));
            }
        }

        return builder.build();
    }

    public static Component skillSlot(SkillSlot slot) {

        TextComponent.Builder builder = text();
        SkilledPlayer player = slot.player();
        if (slot.status() == SkillSlot.Status.IN_USE) {
            builder.append(text("[", ACCENT))
                    .append(text("*", HIGHLIGHT)
                            .hoverEvent(showText(slot.skill().map(skill -> skillInfo(skill.configuredSkill(), player)).orElse(empty()))))
                    .append(text("]", ACCENT));
        } else if (slot.status() == SkillSlot.Status.FREE) {
            return builder.append(text("[", ACCENT)).append(text("O", SUCCESS)).append(text("]", ACCENT))
                    .build().hoverEvent(showText(text("Aktiviere einen Skill um den Skill Slot zu belegen.", NOTE)))
                    .clickEvent(suggestCommand(PlayerCommands.activateSkill(null)));
        } else if (slot.status() == SkillSlot.Status.ELIGIBLE) {
            double cost = SkillsPlugin.instance().getSlotManager().calculateSlotCost(player);
            return builder.append(text("[", ERROR))
                    .append(text("$", Economy.get().has(player.offlinePlayer(), cost) ? SUCCESS : ERROR_ACCENT))
                    .append(text("]", ERROR)).build()
                    .hoverEvent(showText(text("Du kannst diesen Skill Slot für ", NOTE)
                            .append(text(Economy.get().format(cost), Economy.get().has(player.offlinePlayer(), cost) ? SUCCESS : ERROR_ACCENT))
                            .append(text(" kaufen.", NOTE)).append(newline()).append(newline())
                            .append(text("Klicke um den Slot zu kaufen.", NOTE))
                    )).clickEvent(runCommand(PlayerCommands.buySkillSlot()));
        }

        slot.skill().ifPresent(skill -> builder.build().hoverEvent(showText(skillInfo(skill.configuredSkill(), player))));

        return builder.build();
    }

    public static Component activeSkills(SkilledPlayer player) {

        return text().append(text("aktive Skills: ", TEXT))
                .append(text(player.activeSkills().size(), SUCCESS)).append(text("/", DARK_ACCENT))
                .append(text(player.unlockedSkills().size(), ACCENT))
                .build();
    }

    public static Component player(SkilledPlayer player) {

        return text(player.name(), ACCENT, BOLD)
                .hoverEvent(showText(playerInfo(player)));
    }

    public static Component playerInfo(SkilledPlayer player) {

        return text().append(text("--- [ ", DARK_ACCENT))
                .append(text(player.name(), ACCENT))
                .append(text(" ] ---", DARK_ACCENT)).append(newline())
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

        TextComponent header = text("Skills von ", DARK_ACCENT).append(player(player)
                .clickEvent(runCommand("/rcs")));
        return skills(header, player, allSkills, page);
    }

    public static List<Component> skills(Component header, SkilledPlayer player, Collection<ConfiguredSkill> skills, int page) {

        Pagination<ConfiguredSkill> pagination = Pagination.builder()
                .width(Pagination.WIDTH - 6)
                .resultsPerPage(4)
                .build(header, new Pagination.Renderer.RowRenderer<>() {
                    @Override
                    public @NonNull Collection<Component> renderRow(ConfiguredSkill value, int index) {

                        if (value == null) return Collections.singletonList(empty());
                        return skill(value, player, true, true);
                    }
                }, p -> "/rcskills skills " + p + " " + player.name());
        return pagination.render(skills, page);
    }

    public static Component skills(List<PlayerSkill> skills) {

        if (skills.isEmpty()) return empty();

        TextComponent.Builder builder = text();
        for (int i = 0; i < skills.size(); i++) {
            builder.append(text("  - ", TEXT)).append(skill(skills.get(i).configuredSkill(), skills.get(i).player(), true, false));
            if (i != skills.size() - 1) {
                builder.append(newline());
            }
        }

        return builder.build();
    }

    public static Component skill(PlayerSkill skill, boolean showDetails) {

        List<Component> components = skill(skill.configuredSkill(), skill.player(), showDetails, false);
        TextComponent.Builder text = text();
        for (Component component : components) {
            text.append(component).append(newline());
        }
        return text.build();
    }

    public static Component skill(ConfiguredSkill skill, SkilledPlayer player) {

        List<Component> components = skill(skill, player, false, false);
        TextComponent.Builder builder = text();
        for (Component component : components) {
            builder.append(component).append(newline());
        }
        return builder.build();
    }

    public static List<Component> skill(ConfiguredSkill skill, SkilledPlayer player, boolean showDetails, boolean showChildren) {

        ArrayList<Component> result = new ArrayList<>();

        TextColor color = NOTE;
        if (skill.disabled()) {
            color = DISABLED;
        } else if (player != null) {
            if (player.hasActiveSkill(skill)) {
                color = ACTIVE;
            } else if (player.hasSkill(skill)) {
                color = UNLOCKED;
            } else if (player.canBuy(skill)) {
                color = SUCCESS;
            } else {
                color = skill.level() > player.level().getLevel() ? ERROR_ACCENT : ERROR;
            }
        }

        TextColor levelColor = HIGHLIGHT;
        if (player != null) {
            levelColor = player.level().getLevel() >= skill.level() ? SUCCESS : ERROR_ACCENT;
        }

        TextComponent.Builder builder = text();

        if (showDetails) {
            builder.append(text("[", TEXT))
                    .append(text(skill.level(), levelColor))
                    .append(text("] ", TEXT));
        }

        ClickEvent clickEvent = runCommand("/rcskills skill " + skill.id().toString() + " " + (player != null ? player.id().toString() : ""));
        if (player != null && player.hasActiveSkill(skill) && !PlayerSkill.getOrCreate(player, skill).replaced()) {
            builder.append(text(skill.name(), color, BOLD).clickEvent(clickEvent));
        } else {
            builder.append(text(skill.name(), color).clickEvent(clickEvent));
        }

        if (skill.disabled()) {
            return Collections.singletonList(builder.hoverEvent(showText(text("Der Skill ist deaktiviert.", ERROR))).build());
        }

        builder.hoverEvent(skillInfo(skill, player));

        if (player != null && showDetails) {
            PlayerSkill playerSkill = PlayerSkill.getOrCreate(player, skill);
            if (player.canBuy(skill)) {
                builder.append(text(" | ", DARK_ACCENT)).append(text(" [", TEXT).append(text("kaufen", SUCCESS, BOLD)).append(text("]", TEXT))
                        .hoverEvent(costs(playerSkill).append(text("Klicken um den Skill zu kaufen.", NOTE, ITALIC)))
                        .clickEvent(clickEvent(Action.RUN_COMMAND, PlayerCommands.buySkill(player, skill)))
                );
            } else if (playerSkill.active() && playerSkill.enabled()) {
                builder.append(text(" | ", YELLOW)).append(text("[", NOTE)).append(text("aktiv", SUCCESS).hoverEvent(HoverEvent.showText(
                        text("Der Skill ist aktiv.", NOTE).append(newline())
                                .append(text("Gebe ", NOTE))
                                .append(text(PlayerCommands.reset(player), ACCENT, ITALIC))
                                .append(text(" ein um alle deine Skills zurückzusetzen.", NOTE)).append(newline())
                            .append(text("Klicke um ", NOTE, ITALIC).append(text(PlayerCommands.reset(player), ACCENT, ITALIC)))
                            .append(text(" auszuführen.", NOTE, ITALIC))
                        )).clickEvent(suggestCommand(PlayerCommands.reset(player)))
                ).append(text("]", NOTE));
            } else if (playerSkill.unlocked() && !playerSkill.isChild()) {
                if (playerSkill.canActivate()) {
                    builder.append(text(" | ", YELLOW)).append(text("[", SUCCESS)).append(text("aktivieren", INACTIVE, BOLD).hoverEvent(HoverEvent.showText(
                            text("Du besitzt den Skill, er ist aber nicht aktiv.", NOTE).append(newline())
                                    .append(text("Klicke um den Skill zu ", NOTE))
                                    .append(text("aktivieren", SUCCESS))
                                    .append(text(" und ihn einem Slot zuzuweisen.", NOTE)).append(newline())
                                    .append(skillSlots(player))))
                            .clickEvent(runCommand(PlayerCommands.activateSkill(playerSkill)))
                    ).append(text("]", SUCCESS));
                } else if (!playerSkill.replaced()) {
                    Optional<Map.Entry<Integer, SkillPluginConfig.LevelUp>> nextLevelUp = SkillsPlugin.instance().getPluginConfig()
                            .getLevelUpConfig()
                            .getNextLevelUp(player.level().getLevel());

                    TextComponent hover = text("Du besitzt den Skill, kannst ihn aber nicht aktivieren, da du keinen freien Skill Slot hast.", ERROR)
                            .append(newline());
                    if (nextLevelUp.isPresent() && nextLevelUp.get().getValue().getSlots() > 0) {
                        hover.append(text("Mit ", NOTE))
                                .append(text("Level " + nextLevelUp.get().getKey(), HIGHLIGHT))
                                .append(text(" erhältst du ", NOTE))
                                .append(text(nextLevelUp.get().getValue().getSlots() + " Skill Slot(s)", SUCCESS))
                                .append(text(".", NOTE)).append(newline());
                    }
                    hover.append(text("Mit Events und Achievements kannst du jederzeit weitere Skill Slots erhalten.", NOTE, ITALIC));

                    builder.append(text(" | ", DARK_ACCENT)).append(text("[", NOTE))
                            .append(text("aktivieren", ERROR).hoverEvent(showText(hover)))
                            .append(text("]", NOTE));
                }
            }
            if (playerSkill.executable() && playerSkill.active() && !playerSkill.isChild()) {
                builder.append(text(" | ", YELLOW));
                List<ItemBinding> bindings = player.bindings().get(playerSkill);
                TextComponent.Builder hover = text().append(text("Klicke um den Skill auf das Item in deiner Hand zu binden.", NOTE));
                if (!bindings.isEmpty()) {
                    hover.append(newline()).append(text("Bestehende Item Bindings: ", TEXT)).append(newline());
                    for (ItemBinding binding : bindings) {
                        hover.append(text(" - ", TEXT))
                                .append(text(binding.material().getKey().getKey(), ACCENT))
                                .append(text(" (" + binding.action().friendlyName() + ")")).append(newline());
                    }
                }
                builder.append(text("[", NOTE)).append(text("bind", ACCENT, BOLD)
                        .hoverEvent(hover.build())
                        .clickEvent(suggestCommand(PlayerCommands.bindSkill(playerSkill)))
                ).append(text("]", NOTE));
            }
        }

        result.add(builder.build());

        if (skill.isParent() && showChildren) {
            result.addAll(childSkills(skill, player));
        }

        return result;
    }

    private static List<Component> childSkills(ConfiguredSkill skill, SkilledPlayer player) {

        ArrayList<Component> result = new ArrayList<>();
        List<ConfiguredSkill> childSkills = skill.children().stream()
                .filter(ConfiguredSkill::visible)
                .collect(Collectors.toList());
        if (childSkills.size() > 0) {
            for (ConfiguredSkill child : childSkills) {
                TextComponent.Builder childBuilder = text();
                ConfiguredSkill tmp = child.parent();
                while (tmp.isChild()) {
                    childBuilder.append(text(" "));
                    tmp = tmp.parent();
                }
                childBuilder.append(text(" \u2937 ", TEXT, BOLD)); //⤷
                result.add(childBuilder.append(skill(child, player, true, false).stream()
                        .findFirst().orElse(empty())).build());
                result.addAll(childSkills(child, player));
            }
        }
        return result;
    }

    public static Component skillInfo(ConfiguredSkill skill, SkilledPlayer player) {

        TextComponent.Builder builder = text().append(skillInfo(skill));
        if (player != null) {
            PlayerSkill playerSkill = PlayerSkill.getOrCreate(player, skill);
            builder.append(text("Status: ", TEXT))
                    .append(text(playerSkill.configuredSkill().disabled() ? "deaktiviert" : playerSkill.status().localized(), ACCENT))
                    .append(newline())
                    .append(costs(playerSkill)).append(newline())
                    .append(requirements(playerSkill));
            if (playerSkill.executable() && skill.executionConfig().cooldown() > 0) {
                builder.append(newline())
                        .append(text("Verbleibender Cooldown: ", TEXT))
                        .append(text(TimeUtil.formatTime(playerSkill.remainingCooldown()), playerSkill.onCooldown() ? ERROR : SUCCESS));
            }
            if (playerSkill.executable() && !playerSkill.isChild()) {
                builder.append(newline())
                        .append(text("Tipp: ", SUCCESS).append(text("Du kannst den Skill mit ", NOTE))
                                .append(text("/bind " + skill.alias(), ACCENT, ITALIC))
                                .append(text(" auf Gegenstände binden um sie mit einem Rechts- oder Linksklick auszuführen.", NOTE, ITALIC)));
            }
        }
        return builder.build();
    }

    public static Component skillInfo(ConfiguredSkill skill) {

        TextComponent.Builder builder = text().append(text(skill.name(), ACCENT))
                .append(text(" (" + skill.alias() + ")", NOTE, ITALIC)).append(newline())
                .append(text("Level: ", TEXT).append(text(skill.level(), HIGHLIGHT))).append(newline())
                .append(text("Typ: ", TEXT));
        if (SkillsPlugin.instance().getSkillManager().isExecutable(skill)) {
            builder.append(text("AKTIV", SUCCESS, BOLD)).append(newline())
                    .append(text("| ", DARK_ACCENT))
                    .append(text("Cooldown: ", TEXT))
                    .append(text(TimeUtil.formatTime(skill.executionConfig().cooldown()), HIGHLIGHT))
                    .append(text(" | ", DARK_ACCENT))
                    .append(text("Reichweite: ", TEXT)).append(text(skill.executionConfig().range(), HIGHLIGHT))
                    .append(text(" |", DARK_ACCENT)).append(newline());

        } else {
            builder.append(text("PASSIV", DARK_ACCENT)).append(newline());
        }

        if (skill.money() > 0d) {
            builder.append(text("Kosten: ", TEXT))
                    .append(text(Economy.get().format(skill.money()), HIGHLIGHT))
                    .append(newline());
        }
        if (skill.skillpoints() > 0) {
            builder.append(text("Skillpunkte: ", TEXT))
                    .append(text(skill.skillpoints(), HIGHLIGHT))
                    .append(newline());
        }
        return builder
                .append(text("Beschreibung: ", TEXT))
                .append(text(skill.description(), NOTE, ITALIC)).append(newline())
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
            builder.append(text("Vorraussetzungen: ", TEXT)).append(newline());
            for (Requirement requirement : requirements) {
                builder.append(text(" - ", TEXT))
                        .append(text(requirement.name(), requirementColor(requirement, skill.player()))
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
            builder.append(text("Kosten:", TEXT)).append(newline());
            for (Requirement cost : costs) {
                builder.append(text(" - ", TEXT))
                        .append(text(cost.name(), requirementColor(cost, skill.player()))
                                .hoverEvent(showText(requirement(cost, skill.player()))))
                        .append(newline());
            }
        }

        return builder.build();
    }

    public static Component requirement(Requirement requirement, SkilledPlayer player) {

        return text().append(text("--- [ ", DARK_ACCENT))
                .append(text(requirement.name(), requirementColor(requirement, player)))
                .append(text(" ] ---", DARK_ACCENT))
                .append(newline())
                .append(text(requirement.description(), NOTE, ITALIC))
                .build();
    }

    public static Component resultOf(ExecutionResult executionResult) {

        TextComponent.Builder builder = text().append(text("Der Skill ", TEXT)
                .append(skill(executionResult.context().source().playerSkill(), false)));

        switch (executionResult.status()) {
            case SUCCESS:
                return builder.append(text(" wurde ", TEXT)
                        .append(text("erfolgreich", SUCCESS))
                        .append(text(" ausgeführt.", TEXT)))
                        .build();
            case COOLDOWN:
                return builder.append(text(" hat noch einen Cooldown von ", TEXT)
                        .append(text(executionResult.formattedCooldown(), ERROR))
                        .append(text(".", TEXT)))
                        .build();
            case DELAYED:
                return builder.append(text(" wird in ", TEXT)
                        .append(text(executionResult.formattedDelay(), WARNING))
                        .append(text(" ausgeführt.", TEXT)))
                        .build();
            case EXCEPTION:
            case FAILURE:
                return builder.append(text(" konnte nicht ausgeführt werden.", ERROR_ACCENT).append(newline())
                        .append(text("Bei der Ausführung des Skills ist ein Fehler aufgetreten: ", ERROR))
                        .append(text(String.join(",", executionResult.errors()), ERROR)))
                        .build();
        }

        return builder.append(text(" wurde ausgeführt.", TEXT)).build();
    }

    public static TextColor requirementColor(Requirement requirement, SkilledPlayer player) {

        if (player == null) return TEXT;
        return requirement.test(player).success() ? SUCCESS : ERROR;
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
                return ACTIVE;
            case UNLOCKED:
                return UNLOCKED;
            default:
                return TEXT;
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
