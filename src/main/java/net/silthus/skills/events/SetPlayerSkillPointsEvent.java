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
public class SetPlayerSkillPointsEvent extends PlayerEvent implements Cancellable {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final int oldSkillPoints;
    private int newSkillPoints;
    private boolean cancelled;

    public SetPlayerSkillPointsEvent(SkilledPlayer player, int oldSkillPoints, int newSkillPoints) {
        super(player);
        this.oldSkillPoints = oldSkillPoints;
        this.newSkillPoints = newSkillPoints;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
