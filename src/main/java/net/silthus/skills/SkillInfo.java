package net.silthus.skills;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the given class that implements {@link Skill} as a skill.
 * <p>Use this annotation to provide additional information about your skill.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SkillInfo {

    /**
     * The unique identifier of this skill.
     * <p>This is the value that must be used in the config as type.
     *
     * @return the unique type identifier of this skill
     */
    String value();
}
