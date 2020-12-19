package de.raidcraft.skills.skills;

import de.raidcraft.skills.*;
import de.raidcraft.skills.entities.PlayerSkill;
import lombok.NonNull;
import net.silthus.configmapper.ConfigOption;

@SkillInfo("online-time-exp")
public class OnlineTimeExpSkill extends AbstractSkill implements PeriodicAsync {

    public static class Factory implements SkillFactory<OnlineTimeExpSkill> {

        @Override
        public Class<OnlineTimeExpSkill> getSkillClass() {
            return OnlineTimeExpSkill.class;
        }

        @Override
        public @NonNull OnlineTimeExpSkill create(SkillContext context) {
            return new OnlineTimeExpSkill(context);
        }

    }

    @ConfigOption(
            description = {
                    "The interval in seconds the player should get rewarded.",
                    "A value of 900 is 15 minutes. Which means he will get " +
                            "the configured exp every 15 minutes he is online."
            }
    )
    private long interval = 900;

    @ConfigOption(description = "The amount of exp the player gets after " +
            "he played the configured interval.")
    private int exp = 25;

    OnlineTimeExpSkill(SkillContext context) {
        super(context);
    }

    @Override
    public void tickAsync() {

    }
}
