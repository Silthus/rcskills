package net.silthus.skills.commands;


import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.silthus.skills.AddSkillResult;
import net.silthus.skills.ConfiguredSkill;
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
    @Description("{@@rcskills.add-cmd.desc}")
    @CommandPermission("rcskills.admin.skill.add")
    public void addSkill(SkilledPlayer skilledPlayer, ConfiguredSkill skill, @Default("bypass") String mode) {

        AddSkillResult result = skilledPlayer.addSkill(skill, mode.equalsIgnoreCase("bypass"));
        if (result.success()) {
            getCurrentCommandIssuer().sendMessage("{@@rcskills.add-cmd.info}");
        } else {
            getCurrentCommandIssuer().sendMessage(ChatColor.RED + result.errorMessage());
        }
    }

    @Subcommand("remove|del")
    @CommandCompletion("@players @skills")
    @Description("{@@rcskills.remove-cmd.desc}")
    @CommandPermission("rcskills.admin.skill.remove")
    public void removeSkill(SkilledPlayer player, ConfiguredSkill skill) {

        player.removeSkill(skill);
        getCurrentCommandIssuer().sendMessage("{@@rcskills.remove-cmd.info}");
    }

    @Subcommand("reload")
    @Description("{@@rcskills.reload-cmd}}")
    @CommandPermission("rcskills.admin.reload")
    public void reload() {

        getSkillManager().reload();
        getCurrentCommandIssuer().sendMessage("{@@rcskills.reload}");
    }
}
