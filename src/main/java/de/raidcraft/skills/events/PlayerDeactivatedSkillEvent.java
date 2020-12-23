package de.raidcraft.skills.events;

import de.raidcraft.skills.entities.ConfiguredSkill;
import de.raidcraft.skills.entities.PlayerSkill;
import de.raidcraft.skills.entities.SkilledPlayer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.HandlerList;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PlayerDeactivatedSkillEvent extends PlayerEvent {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final PlayerSkill playerSkill;

    public PlayerDeactivatedSkillEvent(SkilledPlayer player, PlayerSkill playerSkill) {
        super(player);
        this.playerSkill = playerSkill;
    }

    public ConfiguredSkill getSkill() {

        return playerSkill.configuredSkill();
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
