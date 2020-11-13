package net.silthus.skills.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.silthus.skills.Skill;
import net.silthus.skills.SkillsPlugin;
import net.silthus.skills.entities.PlayerSkill;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@CommandAlias("rcs|rcskills|skills")
public class SkillsCommand extends BaseCommand {

    private final SkillsPlugin plugin;

    public SkillsCommand(SkillsPlugin plugin) {
        this.plugin = plugin;
    }

    @Default
    @Subcommand("list")
    @Syntax("[--all] [page]")
    @Description("{@@rcskills.list-cmd}")
    public void list(Player player, String[] args) {

        if (args.length == 0) {
            listSkills(player, plugin.getSkillManager().getPlayer(player).skills().stream()
                    .map(playerSkill -> playerSkill.skill().orElse(null))
                    .filter(Objects::nonNull).collect(Collectors.toList()));
        } else if (args[0].equalsIgnoreCase("--all")) {
            listSkills(player, plugin.getSkillManager().loadedSkills().values());
        }
    }

    @Subcommand("add")
    @Description("{@@rcskills.add-cmd}")
    public void addSkill(Player player) {

    }

    @Subcommand("reload")
    @Description("{@@rcskills.reload-cmd}}")
    @CommandPermission("rcskills.cmd.reload")
    public void reload(Player player) {

        plugin.getSkillManager().reload();
        player.sendMessage(ChatColor.GREEN  + "Reload all skills from disk.");
    }

    private void listSkills(CommandSender sender, Collection<Skill> skillList) {

        for (Skill skill : skillList) {
            BaseComponent[] text = new ComponentBuilder().append("[").color(ChatColor.AQUA)
                    .append(skill.name()).color(ChatColor.YELLOW)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder()
                            .append("-=[ ").color(ChatColor.AQUA)
                            .append(skill.name()).color(ChatColor.YELLOW)
                            .append(" (").color(ChatColor.GREEN)
                            .append(skill.identifier())
                            .color(ChatColor.DARK_GRAY)
                            .append(")").color(ChatColor.GREEN)
                            .append(" ]=-").color(ChatColor.AQUA)
                            .append(skill.description()).italic(true).color(ChatColor.GRAY)
                            .create()
                    )))
                    .append("]").color(ChatColor.AQUA)
                    .append(" - ").color(ChatColor.BLACK).create();
            sender.spigot().sendMessage(text);
        }
    }
}
