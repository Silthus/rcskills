package de.raidcraft.skills.art;

import de.raidcraft.skills.entities.SkilledPlayer;
import io.artframework.*;
import io.artframework.annotations.ART;
import io.artframework.annotations.ConfigOption;
import lombok.NonNull;
import org.bukkit.OfflinePlayer;

@ART(
        value = "rcskills:level.add",
        alias = {"addlevel", "addrclevel", "level.add"},
        description = "Adds RC-Level to the player."
)
public class AddExpAction implements Action<OfflinePlayer> {

    @ConfigOption(required = true, position = 0, description = "The amount of level to add to the player.")
    private int amount;

    @Override
    public Result execute(@NonNull Target<OfflinePlayer> target, @NonNull ExecutionContext<ActionContext<OfflinePlayer>> context) {

        SkilledPlayer.getOrCreate(target.source()).addLevel(amount);

        return success();
    }
}
