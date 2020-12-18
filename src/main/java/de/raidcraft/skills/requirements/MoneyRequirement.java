package de.raidcraft.skills.requirements;

import de.raidcraft.economy.wrapper.Economy;
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
@RequirementInfo("money")
@EqualsAndHashCode(callSuper = true)
public class MoneyRequirement extends AbstractRequirement {

    @ConfigOption(required = true)
    private double amount = 0d;

    @Override
    public String name() {

        return String.format(msg(msgIdentifier("name"), "%s"), Economy.get().format(amount));
    }

    @Override
    public String description() {

        return String.format(msg(msgIdentifier("description"), "Du benötigst mindestens %s um diesen Skill kaufen zu können."), Economy.get().format(amount));
    }

    @Override
    protected void loadConfig(ConfigurationSection config) {

        this.amount = config.getDouble("amount", 0d);
    }

    @Override
    public TestResult test(@NonNull SkilledPlayer target) {

        Economy economy = Economy.get();
        return TestResult.of(economy.has(target.getOfflinePlayer(), amount),
                "Du benötigst mindestens " + economy.format(amount) + " um den Skill zu kaufen.");
    }
}
