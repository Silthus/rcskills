package de.raidcraft.skills.events;

import de.raidcraft.skills.entities.SkilledPlayer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SetPlayerSkillSlotsEvent extends PlayerEvent implements Cancellable {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final int oldSkillSlots;
    private int newSkillSlots;
    private boolean cancelled;

    public SetPlayerSkillSlotsEvent(SkilledPlayer player, int oldSkillSlots, int newSkillSlots) {
        super(player);
        this.oldSkillSlots = oldSkillSlots;
        this.newSkillSlots = newSkillSlots;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
