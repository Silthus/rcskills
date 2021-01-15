package de.raidcraft.skills.art;

import de.raidcraft.skills.entities.SkilledPlayer;
import de.raidcraft.skills.events.PlayerLeveledEvent;
import io.artframework.Trigger;
import io.artframework.annotations.ART;
import io.artframework.annotations.ConfigOption;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerTrigger implements Trigger, Listener {

    private static final String LEVEL_UP = "rcskills:level";

    @ConfigOption(description = "The level required by the level up trigger.", position = 0)
    private int level = 1;

    @ART(value = LEVEL_UP, alias = "level", description = "Called when the player levels up in RCSkills.")
    @EventHandler(ignoreCancelled = true)
    public void onLevelUp(PlayerLeveledEvent event) {

        trigger(LEVEL_UP, PlayerTrigger.class, (target, context, playerTrigger) -> {
            if (target.source() instanceof OfflinePlayer) {
                return resultOf(SkilledPlayer.getOrCreate((OfflinePlayer) target.source()).level().getLevel() >= playerTrigger.level);
            }
            return success();
        }, event.getPlayer().offlinePlayer(), event, event.getPlayer());
    }
}
