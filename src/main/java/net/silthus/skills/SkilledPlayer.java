package net.silthus.skills;

import net.silthus.skills.entities.PlayerSkill;

import java.util.List;
import java.util.UUID;

public interface SkilledPlayer {

    UUID id();

    String name();

    List<PlayerSkill> skills();

    AddSkillResult addSkill(Skill skill);

    AddSkillResult addSkill(Skill skill, boolean bypassChecks);

    boolean hasSkill(Skill skill);

    boolean hasSkill(String identifier);
}
