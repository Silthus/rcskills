package de.raidcraft.skills.requirements;

import de.raidcraft.skills.AbstractRequirement;
import de.raidcraft.skills.RequirementInfo;
import de.raidcraft.skills.TestResult;
import de.raidcraft.skills.entities.SkilledPlayer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

import static de.raidcraft.skills.Messages.msg;

@Data
@RequirementInfo(value = "permission")
@EqualsAndHashCode(callSuper = true)
public class PermissionRequirement extends AbstractRequirement {

    private List<String> permissions = new ArrayList<>();

    @Override
    public String name() {

        return String.format(msg(msgIdentifier("name"), "%s"), String.join(",", permissions));
    }

    @Override
    public String description() {

        return String.format(msg(msgIdentifier("description"), "Benötigt das Recht %s um den Skill freizuschalten."), String.join(",", permissions));
    }

    @Override
    public void loadConfig(ConfigurationSection config) {

        if (permissions == null) permissions = new ArrayList<>();
        permissions.addAll(config.getStringList("permissions"));
    }

    public PermissionRequirement add(String permission) {

        this.permissions.add(permission);
        return this;
    }

    @Override
    public TestResult test(@NonNull SkilledPlayer player) {

        Boolean result = player.bukkitPlayer().map(p -> permissions.stream().allMatch(p::hasPermission)).orElse(false);

        return TestResult.of(result, "Du hast nicht genügend Rechte um diesen Skill freizuschalten.");
    }
}
