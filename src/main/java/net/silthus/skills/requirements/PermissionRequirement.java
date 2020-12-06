package net.silthus.skills.requirements;

import lombok.NonNull;
import net.silthus.skills.AbstractRequirement;
import net.silthus.skills.Requirement;
import net.silthus.skills.RequirementType;
import net.silthus.skills.TestResult;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

import static net.silthus.skills.Messages.msg;

@RequirementType("permission")
public class PermissionRequirement extends AbstractRequirement {

    private final List<String> permissions = new ArrayList<>();

    @Override
    public String description() {

        return String.format(msg(msgIdentifier("description"), "Requires the %1$s permission to unlock this skill."), permissions);
    }

    @Override
    public Requirement load(ConfigurationSection config) {

        permissions.clear();
        permissions.addAll(config.getStringList("permissions"));
        return this;
    }

    @Override
    public TestResult test(@NonNull SkilledPlayer player) {

        Boolean result = player.getBukkitPlayer().map(p -> permissions.stream().allMatch(p::hasPermission)).orElse(false);

        return TestResult.of(result, "Du hast nicht genügend Rechte um diesen Skill freizuschalten.");
    }
}
