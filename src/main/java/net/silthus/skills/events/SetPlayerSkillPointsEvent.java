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
public class SetPlayerSkillPointsEvent extends PlayerEvent implements Cancellable {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final PlayerLevel level;
    private final long oldSkillPoints;
    private long newSkillPoints;
    private boolean cancelled;

    public SetPlayerSkillPointsEvent(PlayerLevel level, long oldSkillPoints, long newSkillPoints) {
        super(level.player());
        this.level = level;
        this.oldSkillPoints = oldSkillPoints;
        this.newSkillPoints = newSkillPoints;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
