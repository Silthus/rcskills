package net.silthus.skills.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.silthus.skills.entities.Level;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.event.HandlerList;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class PlayerEvent extends RCSkillsEvent {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final SkilledPlayer player;

    protected PlayerEvent(SkilledPlayer player) {
        this.player = player;
    }

    public Level getPlayerLevel() {

        return getPlayer().level();
    }
}
