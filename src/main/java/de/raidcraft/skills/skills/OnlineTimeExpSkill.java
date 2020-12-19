package de.raidcraft.skills.skills;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import de.raidcraft.skills.*;
import io.ebean.annotation.Transactional;
import lombok.NonNull;
import net.silthus.configmapper.ConfigOption;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.time.Instant;

@SkillInfo(value = "online-time-exp")
public class OnlineTimeExpSkill extends AbstractSkill implements Periodic {

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

    private static final String STORAGE_KEY_ONLINE_TIME = "online-time";
    private static final String STORAGE_KEY_LAST_ONLINE = "last-online";
    private static final String STORAGE_KEY_LAST_PAYOUT = "last-payout";

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

    @ConfigOption
    private String reason = "EXP für Onlinezeit";

    private Instant onlineSince;
    private long lastPayOut;

    OnlineTimeExpSkill(SkillContext context) {
        super(context);
    }

    @Override
    public void apply() {

        onlineSince = Instant.now();
        lastPayOut = context().store().get(STORAGE_KEY_LAST_PAYOUT, long.class, 0L);
    }

    @Override
    public void remove() {

        context().store()
                .set(STORAGE_KEY_ONLINE_TIME, onlineTime())
                .set(STORAGE_KEY_LAST_ONLINE, Instant.now())
                .set(STORAGE_KEY_LAST_PAYOUT, lastPayOut)
                .save();
    }

    @Override
    @Transactional
    public void tick() {

        long onlineTime = onlineTime();
        if (lastPayOut == 0) {
            if (onlineTime < interval) return;
            context().skilledPlayer().addExp(exp, reason).save();
            setLastPayout(onlineTime);
        } else if (onlineTime - lastPayOut >= interval) {
            context().skilledPlayer().addExp(exp, reason).save();
            setLastPayout(onlineTime);
        }
    }

    private void setLastPayout(long time) {

        lastPayOut = time;
        context().store().set(STORAGE_KEY_LAST_PAYOUT, time).save();
    }

    /**
     * @return the total online time in seconds
     */
    private long onlineTime() {

        long onlineTime = context().store().get(STORAGE_KEY_ONLINE_TIME, long.class, 0L);
        onlineTime += Instant.now().getEpochSecond() - onlineSince.getEpochSecond();

        return onlineTime;
    }
}
