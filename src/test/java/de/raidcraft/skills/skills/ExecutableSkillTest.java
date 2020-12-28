package de.raidcraft.skills.skills;

import de.raidcraft.skills.*;

public class ExecutableSkillTest extends AbstractSkill implements Executable {

    public ExecutableSkillTest(SkillContext context) {
        super(context);
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {

        return context.success();
    }
}
