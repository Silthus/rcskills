package de.raidcraft.skills.events;

import de.raidcraft.skills.SkillStatus;
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
public class SkillStatusChangeEvent extends PlayerEvent implements Cancellable {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final PlayerSkill skill;
    private final SkillStatus oldStatus;
    private final SkillStatus status;
    private boolean updateChildren = true;
    private boolean autoAssignSlot = true;
    private boolean replaceParents = true;
    private boolean cancelled;

    public SkillStatusChangeEvent(PlayerSkill skill, SkillStatus oldStatus, SkillStatus status) {
        super(skill.player());
        this.skill = skill;
        this.oldStatus = oldStatus;
        this.status = status;
    }

    @Override
    public HandlerList getHandlers() {

        return handlerList;
    }
}
