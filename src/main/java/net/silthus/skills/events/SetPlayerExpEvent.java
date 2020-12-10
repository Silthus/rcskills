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
public class SetPlayerExpEvent extends PlayerEvent implements Cancellable {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final PlayerLevel level;
    private final long oldExp;
    private long newExp;
    private boolean cancelled;

    public SetPlayerExpEvent(PlayerLevel level, long oldExp, long newExp) {
        super(level.player());
        this.level = level;
        this.oldExp = oldExp;
        this.newExp = newExp;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
