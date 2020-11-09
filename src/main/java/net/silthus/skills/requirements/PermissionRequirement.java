package net.silthus.skills.requirements;

import lombok.NonNull;
import net.silthus.skills.AbstractRequirement;
import net.silthus.skills.Requirement;
import net.silthus.skills.TestResult;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static net.silthus.skills.Messages.msg;

public class PermissionRequirement extends AbstractRequirement {

    private final List<String> permissions = new ArrayList<>();

    public PermissionRequirement() {

        super("permission");
    }

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
    public TestResult test(@NonNull Player player) {

        boolean result = permissions.stream().allMatch(player::hasPermission);
        return TestResult.of(result, msg(msgIdentifier("error"), "You do not have to required permission to unlock this skill."));
    }
}
