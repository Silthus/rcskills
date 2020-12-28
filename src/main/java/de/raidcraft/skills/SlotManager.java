package de.raidcraft.skills;

import de.raidcraft.skills.entities.SkilledPlayer;
import lombok.Data;
import lombok.experimental.Accessors;
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

        return calculate(slotPrice, new CalculationConfig(player));
    }

    public double calculateSlotCost(SkilledPlayer player, int slot) {

        return calculate(slotPrice, new CalculationConfig(player)
                .slots(slot - 1)
        );
    }

    public double calculateSlotResetCost(SkilledPlayer player) {

        return calculate(resetPrice, new CalculationConfig(player));
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
                    "active_slots",
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

    private int calculate(IExpressionEvaluator ee, CalculationConfig config) {

        try {
            return (int) Math.round((double) ee.evaluate(
                    x,
                    y,
                    z,
                    a,
                    b,
                    c,
                    config.level,
                    config.slots,
                    config.activeSlots,
                    config.skills,
                    config.resetCount
            ));
        } catch (InvocationTargetException e) {
            log.severe("failed to calculate costs: " + e.getMessage());
            e.printStackTrace();
            return Integer.MAX_VALUE;
        }
    }

    @Data
    @Accessors(fluent = true)
    static class CalculationConfig {

        int level = 1;
        int slots = 0;
        int activeSlots = 0;
        int skills = 0;
        int resetCount = 0;

        private CalculationConfig(SkilledPlayer player) {
            level = player.level().getLevel();
            slots = player.slotCount();
            activeSlots = player.activeSlotCount();
            skills = player.skillCount();
            resetCount = player.resetCount();
        }

        public CalculationConfig slots(int slots) {

            this.slots = Math.max(slots, 0);
            return this;
        }
    }
}
