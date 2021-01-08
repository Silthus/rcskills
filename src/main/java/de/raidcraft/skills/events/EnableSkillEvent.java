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
public class EnableSkillEvent extends PlayerEvent implements Cancellable {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final PlayerSkill skill;
    private boolean cancelled = false;
    private boolean enableChildren = true;

    public EnableSkillEvent(PlayerSkill skill) {
        super(skill.player());
        this.skill = skill;
    }

    @Override
    public HandlerList getHandlers() {

        return handlerList;
    }
}
