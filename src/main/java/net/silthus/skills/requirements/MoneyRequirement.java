package net.silthus.skills.requirements;

import de.raidcraft.economy.Economy;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.silthus.configmapper.ConfigOption;
import net.silthus.skills.AbstractRequirement;
import net.silthus.skills.RequirementType;
import net.silthus.skills.TestResult;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.configuration.ConfigurationSection;

@Data
@RequirementType("money")
@EqualsAndHashCode(callSuper = true)
public class MoneyRequirement extends AbstractRequirement {

    @ConfigOption(required = true)
    private double amount = 0d;

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
