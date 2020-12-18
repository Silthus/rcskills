package de.raidcraft.skills;

import de.raidcraft.skills.entities.PlayerSkill;
import lombok.Data;

@Data
public abstract class AbstractSkill implements Skill {

    private final PlayerSkill playerSkill;

    protected AbstractSkill(PlayerSkill playerSkill) {

        this.playerSkill = playerSkill;
    }
}
