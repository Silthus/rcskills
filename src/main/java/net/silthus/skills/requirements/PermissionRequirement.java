package net.silthus.skills.requirements;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.silthus.skills.AbstractRequirement;
import net.silthus.skills.RequirementInfo;
import net.silthus.skills.TestResult;
import net.silthus.skills.entities.SkilledPlayer;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

import static net.silthus.skills.Messages.msg;

@Data
@RequirementInfo(value = "permission", hidden = true)
@EqualsAndHashCode(callSuper = true)
public class PermissionRequirement extends AbstractRequirement {

    private List<String> permissions = new ArrayList<>();

    @Override
    public String description() {

        return String.format(msg(msgIdentifier("description"), "Requires the %s permission to unlock this skill."), permissions);
    }

    @Override
    public void loadConfig(ConfigurationSection config) {

        if (permissions == null) permissions = new ArrayList<>();
        permissions.clear();
        permissions.addAll(config.getStringList("permissions"));
    }

    public PermissionRequirement add(String permission) {

        this.permissions.add(permission);
        return this;
    }

    @Override
    public TestResult test(@NonNull SkilledPlayer player) {

        Boolean result = player.getBukkitPlayer().map(p -> permissions.stream().allMatch(p::hasPermission)).orElse(false);

        return TestResult.of(result, "Du hast nicht genügend Rechte um diesen Skill freizuschalten.");
    }
}
