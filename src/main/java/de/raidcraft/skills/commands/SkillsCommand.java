package de.raidcraft.skills.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import de.raidcraft.skills.Messages;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.actions.BuySkillAction;
import de.raidcraft.skills.entities.ConfiguredSkill;
import de.raidcraft.skills.entities.PlayerSkill;
import de.raidcraft.skills.entities.SkilledPlayer;
import net.kyori.adventure.text.event.ClickEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static de.raidcraft.skills.Messages.allRequirements;
import static de.raidcraft.skills.Messages.send;
import static de.raidcraft.skills.Messages.skill;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

@CommandAlias("rcs|rcskills")
public class SkillsCommand extends BaseCommand {

    private final SkillsPlugin plugin;
    private final Map<UUID, BuySkillAction> buyActions = new HashMap<>();

    public SkillsCommand(SkillsPlugin plugin) {
        this.plugin = plugin;
    }

    @Default
    @Subcommand("info")
    @CommandCompletion("@players")
    @CommandPermission("rcskills.player.info")
    @Description("Zeigt Informationen über den Spieler an.")
    public void info(@Conditions("others:perm=player.info") SkilledPlayer skilledPlayer) {

        Messages.send(getCurrentCommandIssuer(), Messages.playerInfo(skilledPlayer));
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
    @CommandCompletion("@skills @players")
    @CommandPermission("rcskills.skill.buy")
    @Description("Kauft den agegebenen Skill, falls möglich.")
    public void buy(ConfiguredSkill skill, @Conditions("others:perm=skill.buy") SkilledPlayer player) {

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
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/rcskills buyconfirm"))
                )
                .append(text(" [NEIN - ABBRECHEN]", RED)
                        .hoverEvent(text("Klicken um den Kauf des Skills abzubrechen.", RED, ITALIC))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/rcskills buyabort"))
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

    @Subcommand("buyconfirm")
    @CommandPermission("rcskills.skill.buy")
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

    @Subcommand("buyabort")
    @CommandPermission("rcskills.skill.buy")
    @Description("Bricht den Kauf eines Skills ab.")
    public void buyAbort() {

        BuySkillAction action = buyActions.remove(getCurrentCommandIssuer().getUniqueId());
        if (action != null) {
            Messages.send(getCurrentCommandIssuer(), text("Der Kauf des Skills ", RED)
                    .append(skill(action.skill(), action.player()))
                    .append(text(" wurde abgebrochen.", RED)));
        }
    }
}
