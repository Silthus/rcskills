package de.raidcraft.skills.events;

import de.raidcraft.skills.entities.Level;
import de.raidcraft.skills.entities.SkilledPlayer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
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
