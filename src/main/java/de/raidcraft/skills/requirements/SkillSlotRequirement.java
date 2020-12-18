package de.raidcraft.skills.requirements;

import de.raidcraft.skills.AbstractRequirement;
import de.raidcraft.skills.RequirementInfo;
import de.raidcraft.skills.TestResult;
import de.raidcraft.skills.entities.SkilledPlayer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.silthus.configmapper.ConfigOption;
import org.bukkit.configuration.ConfigurationSection;

import static de.raidcraft.skills.Messages.msg;

@Data
@RequirementInfo("slots")
@EqualsAndHashCode(callSuper = true)
public class SkillSlotRequirement extends AbstractRequirement {

    @ConfigOption(required = true)
    private int slots = 0;

    @Override
    public String name() {

        return String.format(msg(msgIdentifier("name"), "%s Skill Slot(s)"), slots);
    }

    @Override
    public String description() {

        return String.format(msg(msgIdentifier("description"), "Du benötigst mindestens %s freie(n) Skill Slot um diesen Skill freizuschalten."), slots);
    }

    @Override
    protected void loadConfig(ConfigurationSection config) {

        this.slots = config.getInt("slots", 0);
    }

    @Override
    public TestResult test(@NonNull SkilledPlayer target) {

        return TestResult.of(target.freeSkillSlots() >= slots, "Du benötigst " + slots + " freie(n) Skill Slots um diesen Skill freizuschalten.");
    }
}
