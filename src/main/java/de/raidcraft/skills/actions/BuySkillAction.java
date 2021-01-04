package de.raidcraft.skills.actions;

import de.raidcraft.economy.wrapper.Economy;
import de.raidcraft.skills.entities.ConfiguredSkill;
import de.raidcraft.skills.entities.SkilledPlayer;
import io.ebean.annotation.Transactional;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Map;

@Value
@Accessors(fluent = true)
public class BuySkillAction {

    SkilledPlayer player;
    ConfiguredSkill skill;

    /**
     * Executes the buy skill action, checking if the skill can be bought and subtracting any costs.
     * <p>You can pass bypassChecks true to bypass the cost check and charging of costs.
     *
     * @param bypassChecks true to bypass any requirement and cost checks
     * @return the result of the buy action
     */
    @Transactional
    public AddSkillAction.Result execute(boolean bypassChecks) {

        AddSkillAction.Result addResult = new AddSkillAction(player, skill).execute(bypassChecks);

        if (addResult.failure()) return addResult;

        if (!bypassChecks) {
            player.removeSkillPoints(skill.skillpoints()).save();
            Economy.get().withdrawPlayer(player.offlinePlayer(), skill.money(), "Skill \"" + skill.name() + "\" gekauft.", Map.of(
                    "skill", skill.alias(),
                    "skill_id", skill.id()
            ));
        }

        return addResult;
    }
}
