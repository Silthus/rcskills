package net.silthus.skills.events;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.silthus.skills.actions.AddSkillAction;
import net.silthus.skills.actions.BuySkillAction;
import net.silthus.skills.entities.ConfiguredSkill;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class BuyPlayerSkillEvent extends PlayerEvent implements Cancellable {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final BuySkillAction action;
    private boolean bypassChecks;
    private boolean cancelled;

    public BuyPlayerSkillEvent(BuySkillAction action, boolean bypassChecks) {
        super(action.player());
        this.action = action;
        this.bypassChecks = bypassChecks;
    }

    public ConfiguredSkill getSkill() {

        return getAction().skill();
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
