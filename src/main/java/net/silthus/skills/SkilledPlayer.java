package net.silthus.skills;

import java.util.List;
import java.util.UUID;

public interface SkilledPlayer {

    UUID id();

    String name();

    List<Skill> skills();

    void loadSkills();

    AddSkillResult addSkill(Skill skill);

    AddSkillResult addSkill(Skill skill, boolean bypassChecks);

    boolean hasSkill(Skill skill);

    boolean hasSkill(String identifier);
}
