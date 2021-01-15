package de.raidcraft.skills.art;

import de.raidcraft.skills.entities.SkilledPlayer;
import io.artframework.*;
import io.artframework.annotations.ART;
import io.artframework.annotations.ConfigOption;
import lombok.NonNull;
import org.bukkit.OfflinePlayer;

@ART(value = "rcskills:level", alias = "level", description = "Checks the RC-Level of the player if it is equal or greater than the given.")
public class LevelRequirement implements Requirement<OfflinePlayer> {

    @ConfigOption(required = true, position = 0, description = "The level to check.")
    private int level;

    @Override
    public Result test(@NonNull Target<OfflinePlayer> target, @NonNull ExecutionContext<RequirementContext<OfflinePlayer>> context) {

        return resultOf(SkilledPlayer.getOrCreate(target.source()).level().getLevel() >= level);
    }
}
