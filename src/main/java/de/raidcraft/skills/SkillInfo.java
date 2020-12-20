package de.raidcraft.skills;

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

    /**
     * List any plugins the skill needs here.
     * <p>This is like the depends: [...] clause in the plugin.yml and will
     * check if the plugin exists and is enabled before loading the skill.
     * <p>You still need to fetch the plugin in the load(...) method with Bukkit.getPlugin(...).
     *
     * @return the plugin dependencies of this skill
     */
    String[] depends() default {};

    /**
     * The time in ticks on how often a scheduled task,
     * if any should tick for this skill.
     * <p>This is only the default interval and can be overwritten in the config.
     * <p>It will only have an effect if the skill implements {@link Periodic} or {@link PeriodicAsync}.
     *
     * @return the default task interval
     */
    long taskInterval() default 20L;
}
