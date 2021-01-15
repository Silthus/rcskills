package de.raidcraft.skills.art;

import de.raidcraft.skills.entities.SkilledPlayer;
import io.artframework.*;
import io.artframework.annotations.ART;
import io.artframework.annotations.ConfigOption;
import lombok.NonNull;
import org.bukkit.OfflinePlayer;

@ART(value = "rcskills:skill", alias = "skill", description = "Checks if the player has the given skill.")
public class SkillRequirement implements Requirement<OfflinePlayer> {

    @ConfigOption(required = true, position = 0, description = "The skill to check.")
    private String skill;
    @ConfigOption(position = 1, description = "Set to true to only check skills the player has active.")
    private final boolean active = false;

    @Override
    public Result test(@NonNull Target<OfflinePlayer> target, @NonNull ExecutionContext<RequirementContext<OfflinePlayer>> context) {

        if (active) {
            return resultOf(SkilledPlayer.getOrCreate(target.source()).hasActiveSkill(skill));
        } else {
            return resultOf(SkilledPlayer.getOrCreate(target.source()).hasSkill(skill));
        }
    }
}
