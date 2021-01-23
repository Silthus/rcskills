package de.raidcraft.skills.art;

import de.raidcraft.skills.entities.SkilledPlayer;
import de.raidcraft.skills.events.PlayerLeveledEvent;
import io.artframework.*;
import io.artframework.annotations.ART;
import io.artframework.annotations.ConfigOption;
import lombok.NonNull;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


@ART(value = "rcskills:level", alias = "level", description = "Called when the player levels up in RCSkills.")
public class PlayerLevelTrigger implements Trigger, Listener, Requirement<OfflinePlayer> {

    private final Scope scope;

    @ConfigOption(description = "The level required by the level up trigger.", position = 0)
    private int level = 1;

    public PlayerLevelTrigger(Scope scope) {
        this.scope = scope;
    }

    @EventHandler(ignoreCancelled = true)
    public void onLevelUp(PlayerLeveledEvent event) {

        scope.trigger(PlayerLevelTrigger.class).with(event.getPlayer().offlinePlayer()).execute();
    }

    @Override
    public Result test(@NonNull Target<OfflinePlayer> target, @NonNull ExecutionContext<RequirementContext<OfflinePlayer>> executionContext) {

        return resultOf(SkilledPlayer.getOrCreate(target.source()).level().getLevel() >= level);
    }
}
