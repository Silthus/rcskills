package net.silthus.skills.commands;


import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.silthus.skills.Messages;
import net.silthus.skills.SkillsPlugin;
import net.silthus.skills.actions.AddSkillAction;
import net.silthus.skills.entities.ConfiguredSkill;
import net.silthus.skills.entities.PlayerLevel;
import net.silthus.skills.entities.PlayerSkill;
import net.silthus.skills.entities.SkilledPlayer;

@CommandAlias("rcsa|rcs:admin|rcskills:admin|skills:admin")
@CommandPermission("rcskills.admin.*")
public class AdminCommands extends BaseCommand {

    @Getter
    private final SkillsPlugin plugin;

    public AdminCommands(SkillsPlugin plugin) {
        this.plugin = plugin;
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

    @Subcommand("set")
    public class SetCommands extends BaseCommand {

        @Subcommand("level")
        @CommandAlias("setlevel")
        @CommandCompletion("@players")
        @Description("Setzt das Level des Spielers auf den angegebenen Wert.")
        @CommandPermission("rcskills.admin.set.level")
        public void setLevel(SkilledPlayer player, int level) {

            PlayerLevel playerLevel = player.level().level(level);
            playerLevel.save();
            Messages.send(getCurrentCommandIssuer().getUniqueId(), Messages.setLevel(playerLevel, level));
        }

        @Subcommand("exp|xp")
        @CommandAlias("setxp")
        @CommandCompletion("@players")
        @Description("Setzt die Gesamt Erfahrungspunkte des Spielers auf den angegebenen Wert.")
        @CommandPermission("rcskills.admin.set.exp")
        public void setExp(SkilledPlayer player, int exp) {

            PlayerLevel playerLevel = player.level().exp(exp);
            playerLevel.save();
            Messages.send(getCurrentCommandIssuer().getUniqueId(), Messages.setExp(playerLevel, exp));
        }

        @Subcommand("skillpoints|sp")
        @CommandAlias("setskillpoints")
        @CommandCompletion("@players")
        @Description("Setzt die Skillpunkte des Spielers auf den angegebenen Wert.")
        @CommandPermission("rcskills.admin.set.skillpoints")
        public void setSkillpoints(SkilledPlayer player, int skillpoints) {

            PlayerLevel playerLevel = player.level().skillPoints(skillpoints);
            playerLevel.save();
            Messages.send(getCurrentCommandIssuer().getUniqueId(), Messages.setSkillpoints(playerLevel, skillpoints));
        }
    }

    @Subcommand("remove|del")
    public class RemoveCommands extends BaseCommand {

        @Subcommand("skill")
        @CommandAlias("removeskill")
        @CommandCompletion("@players @skills")
        @Description("Entfernt einen Skill von einem Spieler.")
        @CommandPermission("rcskills.admin.remove.skill")
        public void removeSkill(SkilledPlayer player, ConfiguredSkill skill) {

            PlayerSkill playerSkill = player.removeSkill(skill);
            Messages.send(getCurrentCommandIssuer().getUniqueId(), Messages.removeSkill(playerSkill));
        }
    }

    @Subcommand("reload")
    @Description("Lädt das RCSkills Plugin und alle Konfigurationen neu.")
    @CommandPermission("rcskills.admin.reload")
    public void reload() {

        getPlugin().reload();
        getCurrentCommandIssuer().sendMessage(ChatColor.GREEN + "Das Skillplugin wurde neugeladen.");
    }
}
