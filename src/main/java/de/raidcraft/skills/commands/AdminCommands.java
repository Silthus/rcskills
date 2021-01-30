package de.raidcraft.skills.commands;


import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.google.common.base.Strings;
import de.raidcraft.skills.Messages;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.actions.AddSkillAction;
import de.raidcraft.skills.entities.ConfiguredSkill;
import de.raidcraft.skills.entities.PlayerSkill;
import de.raidcraft.skills.entities.SkillSlot;
import de.raidcraft.skills.entities.SkilledPlayer;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

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
                for (String errorMessage : result.testResult().errorMessages()) {
                    getCurrentCommandIssuer().sendMessage(ChatColor.RED + " - " + errorMessage);
                }
            }
        }

        @Subcommand("level")
        @CommandAlias("addlevel")
        @CommandCompletion("@players")
        @Description("Fügt dem Spieler weitere Level hinzu.")
        @CommandPermission("rcskills.admin.add.level")
        public void addLevel(SkilledPlayer player, int level) {

            player.addLevel(level).save();
            Messages.send(getCurrentCommandIssuer().getUniqueId(), Messages.addLevel(player, level));
            Messages.send(player, Messages.addLevelSelf(player, level));
        }

        @Subcommand("exp|xp")
        @CommandAlias("addxp")
        @CommandCompletion("@players")
        @Description("Fügt dem Spieler Erfahrungspunkte hinzu.")
        @CommandPermission("rcskills.admin.add.exp")
        public void addExp(SkilledPlayer player, int exp) {

            player.addExp(exp, "admin:command").save();
            Messages.send(getCurrentCommandIssuer().getUniqueId(), Messages.addExp(player, exp));
            Messages.send(player, Messages.addExpSelf(player, exp));
        }

        @Subcommand("skillpoints|sp")
        @CommandAlias("addskillpoints")
        @CommandCompletion("@players")
        @Description("Fügt dem Spieler Skillpunkte hinzu.")
        @CommandPermission("rcskills.admin.add.skillpoints")
        public void addSkillpoints(SkilledPlayer player, int skillpoints) {

            player.addSkillPoints(skillpoints).save();
            Messages.send(getCurrentCommandIssuer().getUniqueId(), Messages.addSkillpoints(player, skillpoints));
            Messages.send(player, Messages.addSkillPointsSelf(player, skillpoints));
        }

        @Subcommand("slots|skillslots")
        @CommandAlias("addskillslots")
        @CommandCompletion("@players * ELIGIBLE|FREE")
        @Description("Fügt dem Spieler Skill Slots hinzu.")
        @CommandPermission("rcskills.admin.add.skillslots")
        public void addSkillSlots(SkilledPlayer player, @Default("1") int slots, @Default("ELIGIBLE") SkillSlot.Status status) {

            player.addSkillSlots(slots, status).save();
            Messages.send(getCurrentCommandIssuer().getUniqueId(), Messages.addSkillSlots(player, slots));
            Messages.send(player, Messages.addSkillSlotsSelf(player, slots));
        }

        @Subcommand("reset")
        @CommandCompletion("@players *")
        @Description("Fügt dem Spieler einen kostenlosen Reset hinzu.")
        @CommandPermission("rcskills.admin.add.reset")
        public void addResets(SkilledPlayer player, @Default("1") int resets) {

            player.freeResets(player.freeResets() + resets).save();
            Messages.send(getCurrentCommandIssuer().getUniqueId(), Messages.addFreeResets(player, resets));
            Messages.send(player, Messages.addFreeResetsSelf(player, resets));
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

            player.setLevel(level).save();
            Messages.send(getCurrentCommandIssuer().getUniqueId(), Messages.setLevel(player, level));
        }

        @Subcommand("exp|xp")
        @CommandAlias("setxp")
        @CommandCompletion("@players")
        @Description("Setzt die Gesamt Erfahrungspunkte des Spielers auf den angegebenen Wert.")
        @CommandPermission("rcskills.admin.set.exp")
        public void setExp(SkilledPlayer player, int exp) {

            player.setExp(exp).save();
            Messages.send(getCurrentCommandIssuer().getUniqueId(), Messages.setExp(player, exp));
        }

        @Subcommand("skillpoints|sp")
        @CommandAlias("setskillpoints")
        @CommandCompletion("@players")
        @Description("Setzt die Skillpunkte des Spielers auf den angegebenen Wert.")
        @CommandPermission("rcskills.admin.set.skillpoints")
        public void setSkillpoints(SkilledPlayer player, int skillpoints) {

            player.setSkillPoints(skillpoints).save();
            Messages.send(getCurrentCommandIssuer().getUniqueId(), Messages.setSkillpoints(player, skillpoints));
        }

        @Subcommand("slots|skillslots")
        @CommandAlias("setskillslots")
        @CommandCompletion("@players")
        @Description("Setzt die Skill Slots des Spielers auf den angegebenen Wert.")
        @CommandPermission("rcskills.admin.set.skillslots")
        public void setSkillSlots(SkilledPlayer player, int slots) {

            player.setSkillSlots(slots, SkillSlot.Status.FREE).save();
            Messages.send(getCurrentCommandIssuer().getUniqueId(), Messages.setSkillSlots(player, slots));
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

    @Subcommand("purge")
    @CommandCompletion("@players confirm")
    @Description("Setzt einen Spieler komplett zurück.")
    @CommandPermission("rcskills.admin.purge")
    public void reset(SkilledPlayer player, @Optional String confirm) {

        if (Strings.isNullOrEmpty(confirm) || !confirm.equalsIgnoreCase("confirm")) {
            getCurrentCommandIssuer().sendMessage(ChatColor.RED + "Bist du dir ganz sicher, dass du ALLE Skills, Level, Slots und EXP von " + player.name() + " zurücksetzen willst?");
            getCurrentCommandIssuer().sendMessage(ChatColor.RED + "Dieser Befehl kann nicht rückgängig gemacht werden und der Spieler verliert alles was er sich erspielt hat!");
            getCurrentCommandIssuer().sendMessage(ChatColor.RED + "Wenn du dir ganz sicher bist gebe \"/rcs:admin purge " + player.name() + " confirm\" ein.");
            return;
        }

        player.bukkitPlayer().ifPresent(p -> p.kickPlayer("Dein RCSkills Profil wird zurückgesetzt. Bitte warte kurz..."));

        if (player.delete() && getCurrentCommandIssuer() != null) {
            getCurrentCommandIssuer().sendMessage(ChatColor.GREEN + "Das RCSkills Profil des Spielers " + player.name() + " wurde komplett zurückgesetzt.");
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
