package de.raidcraft.skills.skills;

import de.raidcraft.skills.AbstractSkill;
import de.raidcraft.skills.SkillContext;
import de.raidcraft.skills.SkillFactory;
import de.raidcraft.skills.SkillInfo;

@SkillInfo("none")
public class EmptySkill extends AbstractSkill {

    public static class Factory implements SkillFactory<EmptySkill> {

        @Override
        public Class<EmptySkill> getSkillClass() {

            return EmptySkill.class;
        }

        @Override
        public EmptySkill create(SkillContext context) {

            return new EmptySkill(context);
        }
    }

    public EmptySkill(SkillContext context) {
        super(context);
    }
}
