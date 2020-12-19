package de.raidcraft.skills;

import de.raidcraft.skills.entities.PlayerSkill;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
class DefaultSkillContext implements SkillContext{

    private final PlayerSkill playerSkill;

    DefaultSkillContext(PlayerSkill playerSkill) {
        this.playerSkill = playerSkill;
    }

    @Override
    public PlayerSkill playerSkill() {

        playerSkill.refresh();
        return playerSkill;
    }
}
