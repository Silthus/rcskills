package de.raidcraft.skills;

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
    private double a;
    private double b;
    private double c;
    private IExpressionEvaluator slotPrice;
    private IExpressionEvaluator resetPrice;

    public void load(SkillPluginConfig.SkillSlotConfig config) throws CompileException {

        this.x = config.getX();
        this.y = config.getY();
        this.z = config.getZ();
        this.a = config.getA();
        this.b = config.getB();
        this.c = config.getC();

        slotPrice = parseExpression(config.getSlotPrice());
        resetPrice = parseExpression(config.getResetPrice());
    }

    public double calculateSlotCost(SkilledPlayer player) {

        return calculate(slotPrice, player);
    }

    public double calculateSlotResetCost(SkilledPlayer player) {

        return calculate(resetPrice, player);
    }

    private IExpressionEvaluator parseExpression(String expression) {

        try {
            IExpressionEvaluator ee = new CompilerFactory().newExpressionEvaluator();
            ee.setExpressionType(double.class);
            ee.setParameters(new String[] {
                    "x",
                    "y",
                    "z",
                    "a",
                    "b",
                    "c",
                    "level",
                    "slots",
                    "skills",
                    "resets"
            }, new Class[] {
                    double.class,
                    double.class,
                    double.class,
                    double.class,
                    double.class,
                    double.class,
                    int.class,
                    int.class,
                    int.class,
                    int.class
            });

            ee.cook(expression);
            return ee;
        } catch (CompileException e) {
            log.severe("failed to parse expression \"" + expression + "\": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private int calculate(IExpressionEvaluator ee, SkilledPlayer player) {

        int slots = player.slotCount();
        int skills = player.skillCount();
        int resetCount = player.resetCount();

        try {
            return (int) Math.round((double) ee.evaluate(
                    x,
                    y,
                    z,
                    a,
                    b,
                    c,
                    player.level().getLevel(),
                    slots,
                    skills,
                    resetCount
            ));
        } catch (InvocationTargetException e) {
            log.severe("failed to calculate costs: " + e.getMessage());
            e.printStackTrace();
            return Integer.MAX_VALUE;
        }
    }
}
