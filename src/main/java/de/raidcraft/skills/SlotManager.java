package de.raidcraft.skills;

import de.raidcraft.skills.entities.SkillSlot;
import de.raidcraft.skills.entities.SkilledPlayer;
import lombok.extern.java.Log;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.commons.compiler.IExpressionEvaluator;
import org.codehaus.janino.CompilerFactory;

import java.lang.reflect.InvocationTargetException;

@Log(topic = "RCSkills")
public class SlotManager {

    private double x;
    private double y;
    private double z;
    private IExpressionEvaluator ee;

    public void load(SkillPluginConfig.SkillSlotConfig config) throws CompileException {

        this.x = config.getX();
        this.y = config.getY();
        this.z = config.getZ();

        ee = new CompilerFactory().newExpressionEvaluator();
        ee.setExpressionType(double.class);
        ee.setParameters(new String[] {
                "x",
                "y",
                "z",
                "level",
                "slots",
                "skills"
        }, new Class[] {
                double.class,
                double.class,
                double.class,
                int.class,
                int.class,
                int.class
        });

        ee.cook(config.getPrice());
    }

    public double calculateSlotCost(SkilledPlayer player) {

        int slots = player.slotCount();
        int skills = player.skillCount();

        try {
            return (int) Math.round((double) ee.evaluate(
                    x,
                    y,
                    z,
                    player.level().getLevel(),
                    slots,
                    skills
            ));
        } catch (InvocationTargetException e) {
            log.severe("failed to calculate costs of skill slot: " + e.getMessage());
            e.printStackTrace();
            return Integer.MAX_VALUE;
        }
    }
}
