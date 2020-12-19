package de.raidcraft.skills;

import lombok.NonNull;

import java.util.function.Function;

/**
 * Every skill needs a skill factory that exposes methods on how to create the skill.
 * <p>The factory is what needs to be registered or gets auto detected inside other plugins.
 * <p>Make sure your factory takes no additional arguments to auto register it.
 * If it requires additional arguments you need to register the skill or factory yourself with the {@link SkillManager}.
 * <p>Use the {@link SkillManager#registerSkill(Class, Function)} method to register your skill without a factory.
 * <br>
 * <pre>{@code
 * @SkillInfo("my-skill")
 * public class MySkill extends AbstractSkill {
 *
 *     public static class MySkillFactory implements SkillFactory<MySkill> {
 *
 *         @Override
 *         public Class<MySkill> getSkillClass() {
 *
 *             return MySkill.class;
 *         }
 *
 *         @Override
 *         public MySkill create(SkillContext context) {
 *
 *             return new MySkill(context, MyPlugin.getInstance());
 *         }
 *     }
 *     ...
 * }
 * }</pre>
 *
 * @param <TSkill> the type of the skill this factory registers
 * @see SkillManager#registerSkill(SkillFactory)
 */
public interface SkillFactory<TSkill extends Skill> {

    /**
     * @return the class of the skill that is registered by this factory.
     */
    Class<TSkill> getSkillClass();

    /**
     * Creates a new instance of the skill for the given skill context.
     *
     * @param context the context that was created for this skill
     * @return the created skill instance. must not be null.
     */
    @NonNull TSkill create(SkillContext context);
}
