package de.raidcraft.skills.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import de.raidcraft.economy.wrapper.Economy;
import de.raidcraft.skills.Messages;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.actions.BuySkillAction;
import de.raidcraft.skills.entities.ConfiguredSkill;
import de.raidcraft.skills.entities.PlayerSkill;
import de.raidcraft.skills.entities.SkillSlot;
import de.raidcraft.skills.entities.SkilledPlayer;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

import static de.raidcraft.skills.Messages.allRequirements;
import static de.raidcraft.skills.Messages.send;
import static de.raidcraft.skills.Messages.skill;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

@CommandAlias("rcs|rcskills")
public class PlayerCommands extends BaseCommand {

    public static String buySkill(SkilledPlayer player, ConfiguredSkill skill) {

        return "/rcskills buy skill " + skill.id();
    }

    public static String buySkillSlot() {

        return "/rcskills buy slot";
    }

    public static String reset(SkilledPlayer player) {

        return "/rcskills reset";
    }

    public static String activateSkill(PlayerSkill skill) {

        return "/rcskills activate " + (skill == null ? "" : skill.id());
    }

    public static String buyConfirmSkill() {

        return "/rcskills buy confirm skill";
    }

    public static String buyAbortSkill() {

        return "/rcskills buy abort skill";
    }

    public static String buyConfirmSkillSlot() {

        return "/rcskills buy confirm slot";
    }

    public static String buyAbortSkillSlot() {

        return "/rcskills buy abort slot";
    }

    private final SkillsPlugin plugin;

    public PlayerCommands(SkillsPlugin plugin) {
        this.plugin = plugin;
    }

    @Default
    @Subcommand("info")
    @CommandCompletion("@players")
    @CommandPermission("rcskills.player.info")
    @Description("Zeigt Informationen über den Spieler an.")
    public void info(@Conditions("others:perm=player.info") SkilledPlayer player) {

        Messages.send(getCurrentCommandIssuer(), Messages.playerInfo(player));
        Messages.skills(player, 1).forEach(component -> Messages.send(getCurrentCommandIssuer(), component));
    }

    @HelpCommand
    public void help(CommandSender sender, CommandHelp help) {
        send(sender, text(" ----- [ ", DARK_AQUA)
                .append(text("RCSkills", YELLOW))
                .append(text(" ] -----", DARK_AQUA))
        );
        help.showHelp();
    }

    @Subcommand("skills|list")
    @CommandAlias("skills")
    @CommandCompletion("@players")
    @CommandPermission("rcskills.player.skills")
    @Description("Zeigt alle aktiven und verfügbaren Skills an.")
    public void list(@Conditions("others:perm=player.skills") SkilledPlayer player, @Default("1") int page) {

        Messages.skills(player, page).forEach(component -> Messages.send(getCurrentCommandIssuer(), component));
    }

    @Subcommand("myskills|active")
    @CommandAlias("myskills")
    @CommandCompletion("@players")
    @CommandPermission("rcskills.player.skills")
    @Description("Zeigt alle aktiven Skills an.")
    public void listActiveSkills(@Conditions("others:perm=player.skills") SkilledPlayer player, @Default("1") int page) {

        Messages.skills(player, page, player::hasActiveSkill).forEach(component -> Messages.send(getCurrentCommandIssuer(), component));
    }

    @Subcommand("buy")
    public class BuyCommands extends BaseCommand {

        private final Map<UUID, BuySkillAction> buyActions = new HashMap<>();
        private final Set<UUID> buySlotActions = new HashSet<>();

        @Subcommand("skill")
        @CommandCompletion("@skills @players")
        @CommandPermission("rcskills.buy.skill")
        @Description("Kauft den agegebenen Skill, falls möglich.")
        public void buy(ConfiguredSkill skill, @Conditions("others:perm=buy.skill") SkilledPlayer player) {

            if (player.hasSkill(skill)) {
                throw new ConditionFailedException("Du besitzt den Skill " + skill.name() + " bereits.");
            }

            if (!getCurrentCommandIssuer().hasPermission(SkillsPlugin.BYPASS_REQUIREMENT_CHECKS) && !player.canBuy(skill)) {
                PlayerSkill playerSkill = PlayerSkill.getOrCreate(player, skill);
                Messages.send(getCurrentCommandIssuer(), text("Du kannst den Skill ", RED)
                        .append(skill(playerSkill, false))
                        .append(text(" nicht kaufen:", RED)).append(newline())
                        .append(allRequirements(playerSkill))
                );
                return;
            }

            BuySkillAction buySkillAction = new BuySkillAction(player, skill);
            UUID id = getCurrentCommandIssuer().getUniqueId();
            buyActions.put(id, buySkillAction);

            send(getCurrentCommandIssuer(), text()
                    .append(text("Bist du dir sicher, dass du den Skill ", YELLOW))
                    .append(skill(buySkillAction.skill(), player, false))
                    .append(text(" kaufen möchtest? ", YELLOW))
                    .append(text("[JA - KAUFEN]", GREEN)
                            .hoverEvent(text("Klicken um den Kauf des Skills abzuschließen.", GREEN, ITALIC))
                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, buyConfirmSkill()))
                    )
                    .append(text(" [NEIN - ABBRECHEN]", RED)
                            .hoverEvent(text("Klicken um den Kauf des Skills abzubrechen.", RED, ITALIC))
                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, buyAbortSkill()))
                    )
                    .build()
            );

            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                BuySkillAction skillAction = buyActions.remove(id);
                if (skillAction != null) {
                    Messages.send(id, text("Der Kauf des Skills ", RED)
                            .append(skill(skill, player))
                            .append(text(" ist abgelaufen.", RED)));
                }
            }, plugin.getPluginConfig().getBuyCommandTimeout());
        }

        @Subcommand("slot")
        @CommandCompletion("@players")
        @CommandPermission("rcskills.buy.slot")
        @Description("Kauft einen verfügbaren Skill Slot, falls möglich.")
        public void buySlot(@Conditions("others:perm=buy.slot") SkilledPlayer player) {

            double cost = checkSlotBuy(player);

            UUID id = player.id();
            buySlotActions.add(id);
            send(player, text()
                    .append(text("Bist du dir sicher, dass du einen Skill Slot für ", YELLOW))
                    .append(text(Economy.get().format(cost), RED))
                    .append(text(" kaufen möchtest? ", YELLOW))
                    .append(text("[JA - KAUFEN]", GREEN)
                            .hoverEvent(text("Klicken um den Kauf des Skill Slots abzuschließen.", GREEN, ITALIC))
                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, buyConfirmSkillSlot()))
                    )
                    .append(text(" [NEIN - ABBRECHEN]", RED)
                            .hoverEvent(text("Klicken um den Kauf des Skill Slots abzubrechen.", RED, ITALIC))
                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, buyAbortSkillSlot()))
                    )
                    .build()
            );

            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                if (buySlotActions.remove(id)) {
                    Messages.send(id, text("Der Kauf des Skill Slots ist abgelaufen. ", RED));
                }
            }, plugin.getPluginConfig().getBuyCommandTimeout());
        }

        private double checkSlotBuy(SkilledPlayer player) {

            List<SkillSlot> slots = player.skillSlots().stream().filter(SkillSlot::buyable)
                    .collect(Collectors.toList());

            if (slots.size() < 1) {
                throw new ConditionFailedException("Du hast keinen Skill Slot den du kaufen kannst.");
            }

            double cost = plugin.getSlotManager().calculateSlotCost(player);
            if (!Economy.get().has(player.offlinePlayer(), cost)) {
                throw new ConditionFailedException("Du hast nicht genügend Geld um den Skill Slot zu kaufen. " +
                        "Du benötigst mindestens " + Economy.get().format(cost));
            }
            return cost;
        }

        @Subcommand("confirm")
        public class ConfirmCommands extends BaseCommand {

            @Default
            @Subcommand("skill")
            @CommandPermission("rcskills.buy.skill")
            @Description("Bestätigt den Kauf eines Skills.")
            public void buyConfirm() {

                BuySkillAction buySkillAction = buyActions.remove(getCurrentCommandIssuer().getUniqueId());
                if (buySkillAction == null) {
                    getCurrentCommandIssuer().sendMessage(ChatColor.RED + "Der Zeitraum zum Kaufen des Skills ist abgelaufen bitte gebe den Befehl erneut ein.");
                } else {
                    BuySkillAction.Result result = buySkillAction.execute(getCurrentCommandIssuer().hasPermission(SkillsPlugin.BYPASS_REQUIREMENT_CHECKS));
                    if (result.success()) {
                        Messages.send(getCurrentCommandIssuer(), Messages.buySkill(buySkillAction.player(), result.playerSkill()));
                    } else {
                        Messages.send(getCurrentCommandIssuer(), text("Du kannst den Skill ", RED)
                                .append(skill(result.playerSkill(), false))
                                .append(text(" nicht kaufen:", RED)).append(newline())
                                .append(allRequirements(result.playerSkill()))
                        );
                    }
                }
            }

            @Subcommand("slot")
            @CommandPermission("rcskills.buy.slot")
            @Description("Bestätigt den Kauf eines Skill Slots")
            public void slotConfirm() {

                if (!buySlotActions.remove(getCurrentCommandIssuer().getUniqueId())) {
                    throw new ConditionFailedException("Du hast keine ausstehenden Skill Slot Käufe.");
                }

                SkilledPlayer player = SkilledPlayer.find.byId(getCurrentCommandIssuer().getUniqueId());

                if (player == null)
                    throw new InvalidCommandArgument("Du kannst diesen Befehl nur als Spieler ausführen.");

                double cost = checkSlotBuy(player);

                Economy.get().withdrawPlayer(player.offlinePlayer(), cost, "Skill Slot Kauf", Map.of(
                        "player_id", player.id(),
                        "slot_count", player.slotCount(),
                        "skill_count", player.skillCount(),
                        "skill_points", player.skillPoints(),
                        "free_slots", player.freeSkillSlots()
                ));

                player.skillSlots().stream()
                        .filter(SkillSlot::buyable)
                        .findFirst()
                        .ifPresent(skillSlot -> skillSlot.status(SkillSlot.Status.FREE).save());

                Messages.send(player, text("Du hast erfolgreich einen weiteren Skill Slot gekauft!", GREEN).append(newline())
                        .append(text("Du hast jetzt ", YELLOW)).append(text(player.freeSkillSlots() + " freie(n) Skill Slot(s) ", GREEN))
                        .append(text("die du mit ", YELLOW)).append(text("/skills", GOLD)
                                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/skills"))
                                .hoverEvent(HoverEvent.showText(text("Klicken um /skills auszuführen.", GRAY))))
                        .append(text(" zuweisen kannst.", YELLOW))
                );
            }
        }

        @Subcommand("abort")
        public class AbortCommands extends BaseCommand {

            @Default
            @Subcommand("skill")
            @CommandPermission("rcskills.buy.skill")
            @Description("Bricht den Kauf eines Skills ab.")
            public void buyAbort() {

                BuySkillAction action = buyActions.remove(getCurrentCommandIssuer().getUniqueId());
                if (action != null) {
                    Messages.send(getCurrentCommandIssuer(), text("Der Kauf des Skills ", RED)
                            .append(skill(action.skill(), action.player()))
                            .append(text(" wurde abgebrochen.", RED)));
                }
            }

            @Subcommand("slot")
            @CommandPermission("rcskills.buy.slot")
            @Description("Bricht den Kauf eines Skill Slots ab.")
            public void buyAbortSlot() {

                if (buySlotActions.remove(getCurrentCommandIssuer().getUniqueId())) {
                    Messages.send(getCurrentCommandIssuer(), text("Der Kauf des Skill Slots wurde angebrochen.", RED));
                }
            }
        }
    }

    @Subcommand("activate|assign")
    @CommandCompletion("@unlocked-skills")
    @CommandPermission("rcskills.skill.activate")
    @Description("Weist dem Skill einen Slot zu und aktiviert ihn.")
    public void activate(@Conditions("unlocked|others:perm=skill.activate") PlayerSkill skill) {

        if (!skill.canActivate()) {
            throw new ConditionFailedException("Du kannst den Skill " + skill.name() + " nicht aktivieren. Du hast zu wenig freie Skill Slots.");
        }

        skill.activate();
        getCurrentCommandIssuer().sendMessage(ChatColor.GREEN + " Der Skill " + skill.name() + " wurde erfolgreich aktiviert.");
    }
}
