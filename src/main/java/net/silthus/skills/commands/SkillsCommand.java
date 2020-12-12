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
    @Subcommand("list")
    @CommandCompletion("@players")
    @CommandPermission("rcskills.cmd.skills.list")
    @Description("Zeigt alle Skills des Spielers an.")
    public void list(SkilledPlayer skilledPlayer) {

        Messages.send(getCurrentCommandIssuer().getUniqueId(), Messages.playerInfo(skilledPlayer));
    }
}
