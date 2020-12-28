package de.raidcraft.skills.events;

import de.raidcraft.skills.entities.SkilledPlayer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.HandlerList;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PlayerLeveledEvent extends PlayerEvent {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final int oldLevel;
    private final int newLevel;
    private final long exp;

    public PlayerLeveledEvent(SkilledPlayer player, int oldLevel, int newLevel, long exp) {
        super(player);
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
        this.exp = exp;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
