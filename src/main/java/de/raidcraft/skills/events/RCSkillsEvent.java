package de.raidcraft.skills.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class RCSkillsEvent extends Event {
    @Getter
    private static final HandlerList handlerList = new HandlerList();
}