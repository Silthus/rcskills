package net.silthus.skills.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.annotation.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import net.silthus.skills.Messages;
import net.silthus.skills.SkillsPlugin;
import net.silthus.skills.actions.BuySkillAction;
import net.silthus.skills.entities.ConfiguredSkill;
import net.silthus.skills.entities.PlayerSkill;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.Bukkit;
import org.checkerframework.checker.compilermsgs.qual.CompilerMessageKey;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;
import static net.silthus.skills.Messages.*;

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

        if (!player.canBuy(skill)) {
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
            BuySkillAction.Result result = buySkillAction.execute(false);
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
