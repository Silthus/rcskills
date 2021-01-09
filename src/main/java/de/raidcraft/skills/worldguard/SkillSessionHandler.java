package de.raidcraft.skills.worldguard;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import de.raidcraft.skills.Messages;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.entities.PlayerSkill;
import de.raidcraft.skills.entities.SkilledPlayer;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

import java.util.Set;

public class SkillSessionHandler extends Handler {

    public static final Factory FACTORY = new Factory();
    public static class Factory extends Handler.Factory<SkillSessionHandler> {
        @Override
        public SkillSessionHandler create(Session session) {
            // create an instance of a handler for the particular session
            // if you need to pass certain variables based on, for example, the player
            // whose session this is, do it here
            return new SkillSessionHandler(session);
        }
    }

    /**
     * Create a new handler.
     *
     * @param session The session
     */
    protected SkillSessionHandler(Session session) {
        super(session);
    }

    @Override
    public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {

        for (ProtectedRegion region : entered) {
            StateFlag.State flag = region.getFlag(SkillsPlugin.ALLOW_SKILLS_FLAG);
            if (flag != null) {
                SkilledPlayer skilledPlayer = SkilledPlayer.find.byId(player.getUniqueId());
                if (skilledPlayer != null) {
                    if (flag == StateFlag.State.DENY) {
                        skilledPlayer.activeSkills().forEach(PlayerSkill::disable);
                        Messages.send(skilledPlayer, Component.text("Deine Skills funktionieren in dieser Region nicht und wurden deaktiviert.", NamedTextColor.RED));
                        BukkitAudiences.create(SkillsPlugin.instance()).player(player.getUniqueId()).showTitle(Title.title(
                                Component.text("Skills deaktiviert", NamedTextColor.RED),
                                Component.text("Skills werden in dieser Region deaktiviert.", NamedTextColor.YELLOW)
                        ));
                    }
                }
            }
        }

        for (ProtectedRegion region : exited) {
            StateFlag.State flag = region.getFlag(SkillsPlugin.ALLOW_SKILLS_FLAG);
            if (flag != null) {
                SkilledPlayer skilledPlayer = SkilledPlayer.find.byId(player.getUniqueId());
                if (skilledPlayer != null) {
                    if (flag == StateFlag.State.DENY) {
                        skilledPlayer.activeSkills().forEach(PlayerSkill::enable);
                        Messages.send(skilledPlayer, Component.text("Du hast die Region verlassen und deine Skills funktionieren wieder.", NamedTextColor.GREEN));
                        BukkitAudiences.create(SkillsPlugin.instance()).player(player.getUniqueId()).showTitle(Title.title(
                                Component.text("Skills reaktiviert", NamedTextColor.GREEN),
                                Component.text("Anti-Skills Region verlassen.", NamedTextColor.YELLOW)
                        ));
                    }
                }
            }
        }

        return true;
    }
}
