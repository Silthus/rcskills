package net.silthus.skills.commands;


import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.silthus.skills.AddSkillResult;
import net.silthus.skills.entities.ConfiguredSkill;
import net.silthus.skills.SkillManager;
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
    @CommandCompletion("@players @skills bypass|check")
    @Description("Fügt einen Skill direkt einem Spieler zu.")
    @CommandPermission("rcskills.admin.skill.add")
    public void addSkill(SkilledPlayer skilledPlayer, ConfiguredSkill skill, @Default("bypass") String mode) {

        AddSkillResult result = skilledPlayer.addSkill(skill, mode.equalsIgnoreCase("bypass"));
        if (result.success()) {
            getCurrentCommandIssuer().sendMessage(ChatColor.GREEN + "Der Skill " + skill.name() + "(" + skill.alias() + ") wurde " + skilledPlayer.name() + " erfolgreich zugewiesen.");
        } else {
            getCurrentCommandIssuer().sendMessage(ChatColor.RED + result.errorMessage());
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
