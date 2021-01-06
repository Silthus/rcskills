package de.raidcraft.skills.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.*;
import de.raidcraft.economy.wrapper.Economy;
import de.raidcraft.skills.Messages;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.actions.AddSkillAction;
import de.raidcraft.skills.actions.BuySkillAction;
import de.raidcraft.skills.entities.*;
import net.kyori.adventure.text.event.ClickEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.*;
import java.util.stream.Collectors;

import static de.raidcraft.skills.Messages.*;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

@CommandAlias("rcs|skills|rcskills")
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

    public static String confirmReset() {

        return "/rcskills confirmreset";
    }

    public static String abortReset() {

        return "/rcskills abortreset";
    }

    public static String bindSkill(PlayerSkill skill) {

        return "/bind " + skill.alias() + " ";
    }

    public static String unbind(ItemBinding binding) {

        return "/unbind " + binding.action().name() + " " + binding.material().getKey().getKey();
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
    public void info(@Conditions("others:perm=player.info") SkilledPlayer player, @Default("1") int page) {

        Messages.send(getCurrentCommandIssuer(), Messages.playerInfo(player));
        Messages.skills(player, page).forEach(component -> Messages.send(getCurrentCommandIssuer(), component));
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
    @CommandCompletion("@players")
    @CommandPermission("rcskills.player.skills")
    @Description("Zeigt alle aktiven und verfügbaren Skills an.")
    public void list(@Conditions("others:perm=player.skills") SkilledPlayer player, @Default("1") int page) {

        Messages.skills(player, page).forEach(component -> Messages.send(getCurrentCommandIssuer(), component));
    }

    @Subcommand("use|cast|execute")
    @CommandCompletion("@executable-skills")
    @CommandPermission("rcskills.skill.execute")
    @Description("Führt den Skill aus.")
    public void use(PlayerSkill skill) {

        skill.execute(result -> send(getCurrentCommandIssuer(), Messages.resultOf(result)));
    }

    @Subcommand("bind")
    @CommandAlias("bind")
    @CommandCompletion("@executable-skills @bind-actions")
    @CommandPermission("rcskills.skill.bind")
    @Description("Bindet den skill auf das Item in deiner Hand.")
    public void bind(@Conditions("active|executable") PlayerSkill skill, ItemBinding.Action action) {

        if (!getCurrentCommandIssuer().isPlayer()) {
            throw new ConditionFailedException("Dieser Befehl kann nur als Spieler ausgeführt werden.");
        }

        SkilledPlayer skilledPlayer = skill.player();
        Material material = ((Player) getCurrentCommandIssuer().getIssuer()).getInventory().getItemInMainHand().getType();

        ItemBindings bindings = skilledPlayer.bindings();

        Optional<ItemBinding> itemBinding = bindings.get(material, action);
        if (itemBinding.isPresent()) {
            send(getCurrentCommandIssuer(), text("Es besteht bereits ein Binding für den Skill ", RED)
                    .append(skill(itemBinding.get().skill(), false))
                    .append(text(". Entferne das Binding mit ", RED))
                    .append(text("/rcs unbind " + material.getKey().getKey() + " " + action.name(), GOLD))
                    .hoverEvent(showText(text("Klicke um das Binding zu entfernen.", GRAY, ITALIC)))
                    .clickEvent(runCommand("/rcs unbind " + material.getKey().getKey() + " " + action.name()))
            );
            return;
        }

        bindings.bind(skill, material, action);
        plugin.getBindingListener().getUpdateBindings().accept(skilledPlayer.id());

        send(getCurrentCommandIssuer(), text("Der Skill ", YELLOW).append(skill(skill, false))
                .append(text(" wurde erfolgreich mit einem ", YELLOW))
                .append(text(action.friendlyName(), AQUA))
                .append(text(" auf ", YELLOW))
                .append(text(material.getKey().getKey(), AQUA))
                .append(text(" gebunden.", YELLOW))
        );
    }

    @Subcommand("unbind")
    @CommandAlias("unbind")
    @CommandCompletion("@bind-actions @materials")
    @CommandPermission("rcskills.skill.bind")
    @Description("Entfernt alle Bindings für das Item in deiner Hand.")
    public void unbind(@co.aikar.commands.annotation.Optional ItemBinding.Action action,
                       @co.aikar.commands.annotation.Optional Material material,
                       @Flags("self") SkilledPlayer player) {

        if (!getCurrentCommandIssuer().isPlayer()) {
            throw new ConditionFailedException("Dieser Befehl kann nur als Spieler ausgeführt werden.");
        }

        if (material == null) {
            material = ((Player) getCurrentCommandIssuer().getIssuer()).getInventory().getItemInMainHand().getType();
        }

        ItemBindings bindings = player.bindings();
        if (bindings.contains(material)) {
            bindings.unbind(material, action);
            plugin.getBindingListener().getUpdateBindings().accept(player.id());
            if (action == null) {
                send(getCurrentCommandIssuer(), text("Alle Bindings auf dem Item ", GREEN).append(text(material.getKey().getKey(), AQUA)).append(text(" wurden entfernt.", GREEN)));
            } else {
                send(getCurrentCommandIssuer(), text("Das ")
                        .append(text(action.friendlyName(), DARK_AQUA))
                        .append(text(" Binding auf dem Item ", GREEN))
                        .append(text(material.getKey().getKey(), AQUA))
                        .append(text(" wurde entfernt.", GREEN)));
            }
        } else {
            send(getCurrentCommandIssuer(), text("Du hast keine Bindings auf dem Item ", RED).append(text(material.getKey().getKey(), AQUA)).append(text(".", RED)));
        }
    }

    @CommandAlias("unbindall")
    @Subcommand("clearbindings|unbindall")
    @CommandPermission("rcskills.skill.bind")
    @Description("Entfernt alle deine Skill Item Bindings.")
    public void unbindAll(SkilledPlayer player) {

        player.bindings().clear();
        send(getCurrentCommandIssuer(), text("Alle deine Bindings wurden erfolgreich entfernt.", GREEN));
        plugin.getBindingListener().getUpdateBindings().accept(player.id());
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
                    .append(skill(buySkillAction.skill(), player, false, false))
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
                    AddSkillAction.Result result = buySkillAction.execute(getCurrentCommandIssuer().hasPermission(SkillsPlugin.BYPASS_REQUIREMENT_CHECKS));
                    if (result.success()) {
                        Messages.send(getCurrentCommandIssuer(), Messages.buySkill(buySkillAction.player(), result.playerSkill()));
                        list(result.playerSkill().player(), 1);
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
                        .append(text("die du mit ", YELLOW)).append(text("/rcs", GOLD)
                                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/rcs"))
                                .hoverEvent(showText(text("Klicken um /rcs auszuführen.", GRAY))))
                        .append(text(" zuweisen kannst.", YELLOW))
                );
                list(player, 1);
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

        if (skill.configuredSkill().disabled()) {
            throw new ConditionFailedException("Du kannst den Skill " + skill.name() + " nicht aktivieren. Er ist fehlerhaft und deaktiviert.");
        }
        if (skill.active()) {
            throw new ConditionFailedException("Du kannst den Skill " + skill.name() + " nicht aktivieren. Er ist bereits aktiviert.");
        }
        if (!skill.canActivate()) {
            throw new ConditionFailedException("Du kannst den Skill " + skill.name() + " nicht aktivieren. Du hast zu wenig freie Skill Slots.");
        }

        if (skill.activate()) {
            getCurrentCommandIssuer().sendMessage(ChatColor.GREEN + " Der Skill " + skill.name() + " wurde erfolgreich aktiviert.");
            info(skill.player(), 1);
        } else {
            getCurrentCommandIssuer().sendMessage(ChatColor.RED + "Der Skill konnte nicht aktiviert werden.");
        }
    }

    private final Map<UUID, SkilledPlayer> resetList = new HashMap<>();

    @Subcommand("reset")
    @CommandCompletion("@players")
    @CommandPermission("rcskills.reset")
    @Description("Setzt alle Skill Slot Zuweisungen zurück.")
    public void resetSkills(@Conditions("others:perm=reset") SkilledPlayer player) {

        double cost = checkSlotReset(player);
        UUID id = getCurrentCommandIssuer().getUniqueId();
        resetList.put(id, player);
        send(player, text()
                .append(text("Bist du dir sicher, dass du alle deine Skill Slots für ", YELLOW))
                .append(text(Economy.get().format(cost), RED))
                .append(text(" zurücksetzen möchtest? ", YELLOW))
                .append(text("[JA - KAUFEN]", GREEN)
                        .hoverEvent(text("Klicken um das Zurücksetzen deiner Skill Slots abzuschließen.", GREEN, ITALIC))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, confirmReset()))
                )
                .append(text(" [NEIN - ABBRECHEN]", RED)
                        .hoverEvent(text("Klicken um das Zurücksetzen deiner Skill Slots abzubrechen.", RED, ITALIC))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, abortReset()))
                ).append(newline())
                .append(text("Du behältst alle deine gekauften Skills und Skill Slots, kannst danach aber die Skills neu auf die Skill Slots verteilen.", GRAY, ITALIC))
                .build()
        );

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            SkilledPlayer skilledPlayer = resetList.remove(id);
            if (skilledPlayer != null) {
                Messages.send(id, text("Das Zurücksetzen der Skill Slots ist abgelaufen. ", RED));
            }
        }, plugin.getPluginConfig().getBuyCommandTimeout());
    }

    @Subcommand("confirmreset")
    @CommandPermission("rcskills.reset")
    @Description("Bestätigt das Zurücksetzen aller Skill Slot Zuweisungen.")
    public void resetConfirm() {

        SkilledPlayer player = resetList.remove(getCurrentCommandIssuer().getUniqueId());
        if (player == null) {
            throw new InvalidCommandArgument("Das Zurücksetzen der Skill Slots ist bereits abgelaufen. Bitte führe den Befehl erneut aus.");
        }

        double cost = checkSlotReset(player);
        Economy.get().withdrawPlayer(player.offlinePlayer(), cost, "Skill Slot Reset", Map.of(
                "player_id", player.id(),
                "slot_count", player.slotCount(),
                "skill_count", player.skillCount(),
                "skill_points", player.skillPoints(),
                "free_slots", player.freeSkillSlots(),
                "reset_count", player.resetCount()
        ));
        player.resetSkillSlots();

        send(player, text("Deine Skill Slots wurden erfolgreich zurückgesetzt."));
        info(player, 1);
    }

    @Subcommand("abortreset")
    @CommandPermission("rcskills.reset")
    @Description("Bricht das Zurücksetzen aller Skill Slot Zuweisungen ab.")
    public void resetAbort() {

        SkilledPlayer player = resetList.remove(getCurrentCommandIssuer().getUniqueId());
        if (player != null) {
            send(getCurrentCommandIssuer(), text("Das Zurücksetzen der Skill Slots wurde abgebrochen.", RED));
        }
    }

    private double checkSlotReset(SkilledPlayer player) {

        if (player.skillSlots().stream().filter(skillSlot -> skillSlot.status() == SkillSlot.Status.IN_USE).count() < 1) {
            throw new ConditionFailedException("Du hast keine Skill Slots die in Benutzung sind.");
        }

        double cost = plugin.getSlotManager().calculateSlotResetCost(player);
        if (!Economy.get().has(player.offlinePlayer(), cost)) {
            throw new ConditionFailedException("Du hast nicht genügend Geld um deine Skill Slots zurückzusetzen. Du benötigst " + Economy.get().format(cost) + ".");
        }

        return cost;
    }
}
