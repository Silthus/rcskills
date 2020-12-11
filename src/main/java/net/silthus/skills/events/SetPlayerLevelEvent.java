package net.silthus.skills.events;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.silthus.skills.entities.PlayerLevel;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SetPlayerLevelEvent extends PlayerEvent implements Cancellable {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final PlayerLevel level;
    private final int oldLevel;
    private int newLevel;
    private boolean cancelled;

    public SetPlayerLevelEvent(PlayerLevel level, int oldLevel, int newLevel) {
        super(level.player());
        this.level = level;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
