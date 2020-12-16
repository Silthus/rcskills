package net.silthus.skills.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.md_5.bungee.api.ChatColor;
import net.silthus.skills.Messages;
import net.silthus.skills.SkillsPlugin;
import net.silthus.skills.actions.BuySkillAction;
import net.silthus.skills.entities.ConfiguredSkill;
import net.silthus.skills.entities.SkilledPlayer;

@CommandAlias("rcs|rcskills")
public class SkillsCommand extends BaseCommand {

    private final SkillsPlugin plugin;

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

    @Subcommand("list|skills")
    @CommandAlias("skills")
    @CommandCompletion("@players")
    @CommandPermission("rcskills.player.skills")
    @Description("Zeigt alle aktiven und verfügbaren Skills an.")
    public void list(@Conditions("others:perm=player.skills") SkilledPlayer player, @Default("1") int page) {

        Messages.skills(player, page).forEach(component -> Messages.send(getCurrentCommandIssuer(), component));
    }

    @Subcommand("buy")
    @CommandCompletion("@skills @players")
    @CommandPermission("rcskills.skill.buy")
    @Description("Kauft den agegebenen Skill, falls möglich.")
    public void buy(ConfiguredSkill skill, @Conditions("others:perm=skill.buy") SkilledPlayer player) {

        BuySkillAction.Result result = new BuySkillAction(player, skill).execute(false);
        if (result.success()) {
            Messages.send(getCurrentCommandIssuer(), Messages.buySkill(player, result.playerSkill()));
        } else {
            getCurrentCommandIssuer().sendMessage(ChatColor.RED + "Der Skill " + skill.name() + " kann nicht gekauft werden: " + result.error());
        }
    }
}
