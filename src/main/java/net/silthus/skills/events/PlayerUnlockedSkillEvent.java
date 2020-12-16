package net.silthus.skills.events;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.silthus.skills.entities.ConfiguredSkill;
import net.silthus.skills.entities.PlayerSkill;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.event.HandlerList;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PlayerUnlockedSkillEvent extends PlayerEvent {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final PlayerSkill playerSkill;

    public PlayerUnlockedSkillEvent(SkilledPlayer player, PlayerSkill playerSkill) {
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
