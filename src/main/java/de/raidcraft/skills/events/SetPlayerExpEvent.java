package de.raidcraft.skills.events;

import de.raidcraft.skills.entities.Level;
import de.raidcraft.skills.entities.SkilledPlayer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SetPlayerExpEvent extends PlayerEvent implements Cancellable {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final long oldExp;
    private long newExp;
    private int level;
    private String reason;
    private boolean cancelled;

    public SetPlayerExpEvent(SkilledPlayer player, long oldExp, long newExp, int level, String reason) {
        super(player);
        this.oldExp = oldExp;
        this.newExp = newExp;
        this.level = level;
        this.reason = reason;
    }

    public Level getPlayerLevel() {

        return getPlayer().level();
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
