package de.raidcraft.skills.events;

import de.raidcraft.skills.entities.PlayerSkill;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class DisableSkillEvent extends PlayerEvent implements Cancellable {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final PlayerSkill skill;
    private boolean cancelled = false;
    private boolean disableChildren = true;

    public DisableSkillEvent(PlayerSkill skill) {
        super(skill.player());
        this.skill = skill;
    }

    @Override
    public HandlerList getHandlers() {

        return handlerList;
    }
}
