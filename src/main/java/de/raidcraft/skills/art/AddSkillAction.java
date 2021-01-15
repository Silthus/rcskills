package de.raidcraft.skills.art;

import de.raidcraft.skills.entities.ConfiguredSkill;
import de.raidcraft.skills.entities.SkilledPlayer;
import io.artframework.*;
import io.artframework.annotations.ART;
import io.artframework.annotations.ConfigOption;
import lombok.NonNull;
import org.bukkit.OfflinePlayer;

import java.util.Optional;

@ART(value = "rcskills:skill.add", alias = {"addskill", "skill.add"}, description = "Adds a skill to the player.")
public class AddSkillAction implements Action<OfflinePlayer> {

    @ConfigOption(required = true, description = "The alias of the skill that should be added to the player.", position = 0)
    private String skill;

    @ConfigOption(description = "Set to true if you want to bypass the requirement checks of the skill", position = 1)
    private final boolean bypass = false;

    @Override
    public Result execute(@NonNull Target<OfflinePlayer> target, @NonNull ExecutionContext<ActionContext<OfflinePlayer>> context) {

        Optional<ConfiguredSkill> skill = ConfiguredSkill.findByAliasOrName(this.skill);
        if (skill.isEmpty()) {
            return failure("Skill with alias " + this.skill + " not found!");
        }

        de.raidcraft.skills.actions.AddSkillAction action = new de.raidcraft.skills.actions.AddSkillAction(SkilledPlayer.getOrCreate(target.source()), skill.get());

        de.raidcraft.skills.actions.AddSkillAction.Result result = action.execute(bypass);

        return resultOf(result.success(), result.error());
    }
}
