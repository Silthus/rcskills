package net.silthus.skills.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.silthus.skills.Messages;
import net.silthus.skills.SkillsPlugin;
import net.silthus.skills.entities.SkilledPlayer;

@CommandAlias("rcs|rcskills|skills")
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

        Messages.send(getCurrentCommandIssuer().getUniqueId(), Messages.playerInfo(skilledPlayer));
    }
}
