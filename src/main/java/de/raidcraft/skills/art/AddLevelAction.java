package de.raidcraft.skills.art;

import de.raidcraft.skills.entities.SkilledPlayer;
import io.artframework.*;
import io.artframework.annotations.ART;
import io.artframework.annotations.ConfigOption;
import lombok.NonNull;
import org.bukkit.OfflinePlayer;

@ART(
        value = "rcskills:exp.add",
        alias = {"addexp", "addrcexp", "addxp", "xp", "exp", "rcxp", "rcexp", "addrcxp"},
        description = "Adds RC-EXP to the player."
)
public class AddLevelAction implements Action<OfflinePlayer> {

    @ConfigOption(required = true, position = 0, description = "The amount of exp to add to the player.")
    private int amount;
    @ConfigOption(position = 1, description = "The reason for adding exp.")
    private String reason = "ART-Action";

    @Override
    public Result execute(@NonNull Target<OfflinePlayer> target, @NonNull ExecutionContext<ActionContext<OfflinePlayer>> context) {

        SkilledPlayer.getOrCreate(target.source()).addExp(amount, reason);

        return success();
    }
}
