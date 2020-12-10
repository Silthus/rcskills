package net.silthus.skills.commands;


import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.md_5.bungee.api.ChatColor;
import net.silthus.skills.Messages;
import net.silthus.skills.SkillManager;
import net.silthus.skills.SkillsPlugin;
import net.silthus.skills.actions.AddSkillAction;
import net.silthus.skills.entities.ConfiguredSkill;
import net.silthus.skills.entities.PlayerLevel;
import net.silthus.skills.entities.SkilledPlayer;

@CommandAlias("rcsa|rcs:admin|rcskills:admin|skills:admin")
@CommandPermission("rcskills.admin.*")
public class AdminCommands extends BaseCommand {

    @Getter
    private final SkillManager skillManager;

    public AdminCommands(SkillManager skillManager) {
        this.skillManager = skillManager;
    }

    @Subcommand("add")
    public class AddCommands extends BaseCommand {

        @Subcommand("skill")
        @CommandAlias("addskill")
        @CommandCompletion("@players @skills bypass|check")
        @Description("Fügt einen Skill direkt einem Spieler zu.")
        @CommandPermission("rcskills.admin.add.skill")
        public void addSkill(SkilledPlayer skilledPlayer, ConfiguredSkill skill, @Default("bypass") String mode) {

            AddSkillAction.Result result = new AddSkillAction(skilledPlayer, skill).execute(mode.equalsIgnoreCase("bypass"));
            if (result.success()) {
                Messages.send(skilledPlayer.id(), Messages.addSkill(skilledPlayer, result.playerSkill()));
            } else {
                getCurrentCommandIssuer().sendMessage(ChatColor.RED + result.error());
            }
        }

        @Subcommand("level")
        @CommandAlias("addlevel")
        @CommandCompletion("@players")
        @Description("Fügt dem Spieler weitere Level hinzu.")
        @CommandPermission("rcskills.admin.add.level")
        public void addLevel(SkilledPlayer player, int level) {

            PlayerLevel playerLevel = player.level().addLevel(level);
            playerLevel.save();
            Messages.send(getCurrentCommandIssuer().getUniqueId(), Messages.addLevel(playerLevel, level));
        }

        @Subcommand("exp|xp")
        @CommandAlias("addxp")
        @CommandCompletion("@players")
        @Description("Fügt dem Spieler Erfahrungspunkte hinzu.")
        @CommandPermission("rcskills.admin.add.exp")
        public void addExp(SkilledPlayer player, int exp) {

            PlayerLevel playerLevel = player.level().addExp(exp);
            playerLevel.save();
            Messages.send(getCurrentCommandIssuer().getUniqueId(), Messages.addExp(playerLevel, exp));
        }

        @Subcommand("skillpoints|sp")
        @CommandAlias("addskillpoints")
        @CommandCompletion("@players")
        @Description("Fügt dem Spieler Skillpunkte hinzu.")
        @CommandPermission("rcskills.admin.add.skillpoints")
        public void addSkillpoints(SkilledPlayer player, int skillpoints) {

            PlayerLevel playerLevel = player.level().addSkillPoints(skillpoints);
            playerLevel.save();
            Messages.send(getCurrentCommandIssuer().getUniqueId(), Messages.addSkillpoints(playerLevel, skillpoints));
        }
    }


    @Subcommand("remove|del")
    @CommandCompletion("@players @skills")
    @Description("Entfernt einen Skill von einem Spieler.")
    @CommandPermission("rcskills.admin.skill.remove")
    public void removeSkill(SkilledPlayer player, ConfiguredSkill skill) {

        player.removeSkill(skill);
        getCurrentCommandIssuer().sendMessage(ChatColor.GREEN + "Der Skill " + skill.name() + "(" + skill.alias() + ") wurde von " + player.name() + " erfolgreich entfernt.");
    }

    @Subcommand("reload")
    @Description("Lädt das RCSkills Plugin und alle Konfigurationen neu.")
    @CommandPermission("rcskills.admin.reload")
    public void reload() {

        getSkillManager().reload();
        getCurrentCommandIssuer().sendMessage(ChatColor.GREEN + "Das Skillplugin wurde neugeladen.");
    }
}