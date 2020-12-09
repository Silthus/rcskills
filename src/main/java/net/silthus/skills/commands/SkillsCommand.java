package net.silthus.skills.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.silthus.skills.SkillsPlugin;
import net.silthus.skills.entities.ConfiguredSkill;
import net.silthus.skills.entities.PlayerSkill;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.stream.Collectors;

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
    public void list(Player player, SkilledPlayer skilledPlayer) {

        player.spigot().sendMessage(listSkills(skilledPlayer.skills().stream()
                .filter(PlayerSkill::unlocked)
                .collect(Collectors.toList())));
    }

    private BaseComponent[] listSkills(Collection<PlayerSkill> skills) {

        ComponentBuilder builder = new ComponentBuilder();
        for (PlayerSkill playerSkill : skills) {
            ConfiguredSkill skill = playerSkill.skill();
            builder.append("[").color(ChatColor.AQUA)
                    .append(skill.name()).color(ChatColor.YELLOW)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder()
                            .append("-=[ ").color(ChatColor.AQUA)
                            .append(skill.name()).color(ChatColor.YELLOW)
                            .append(" (").color(ChatColor.GREEN)
                            .append(skill.alias())
                            .color(ChatColor.DARK_GRAY)
                            .append(")").color(ChatColor.GREEN)
                            .append(" ]=-\n").color(ChatColor.AQUA)
                            .append(skill.description()).italic(true).color(ChatColor.GRAY)
                            .create()
                    )))
                    .append("]").color(ChatColor.AQUA)
                    .append(" - \n").color(ChatColor.BLACK).create();
        }
        return builder.create();
    }
}
