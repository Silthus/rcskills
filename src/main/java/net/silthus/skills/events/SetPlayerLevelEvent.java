package net.silthus.skills.events;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SetPlayerLevelEvent extends PlayerEvent implements Cancellable {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final int oldLevel;
    private int newLevel;
    private long exp;
    private boolean cancelled;

    public SetPlayerLevelEvent(SkilledPlayer player, int oldLevel, int newLevel, long exp) {
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
