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

    private final PlayerLevel playerLevel;
    private final long oldExp;
    private long newExp;
    private int level;
    private String reason;
    private boolean cancelled;

    public SetPlayerExpEvent(PlayerLevel playerLevel, long oldExp, long newExp, int level, String reason) {
        super(playerLevel.player());
        this.playerLevel = playerLevel;
        this.oldExp = oldExp;
        this.newExp = newExp;
        this.level = level;
        this.reason = reason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
