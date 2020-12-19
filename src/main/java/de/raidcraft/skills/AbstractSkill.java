package de.raidcraft.skills;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public abstract class AbstractSkill implements Skill {

    private final SkillContext context;

    protected AbstractSkill(SkillContext context) {

        this.context = context;
    }
}
