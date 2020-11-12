package net.silthus.skills.skills;

import net.silthus.skills.AbstractSkill;
import net.silthus.skills.SkillType;
import net.silthus.skills.SkillsPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@SkillType("permission")
public class PermissionSkill extends AbstractSkill {

    private List<String> permissions = new ArrayList<>();
    private final Map<UUID, List<PermissionAttachment>> attachments = new HashMap<>();

    @Override
    public void loadSkill(ConfigurationSection config) {

        this.permissions = config.getStringList("permissions");
    }

    @Override
    public void apply(Player player) {

        List<PermissionAttachment> attachments = this.attachments.getOrDefault(player.getUniqueId(), new ArrayList<>());
        attachments
                .addAll(permissions.stream()
                .map(permission -> player.addAttachment(SkillsPlugin.getInstance(), permission, true))
                .collect(Collectors.toList()));
        this.attachments.put(player.getUniqueId(), attachments);
    }

    @Override
    public void remove(Player player) {

        attachments.getOrDefault(player.getUniqueId(), new ArrayList<>()).forEach(player::removeAttachment);
    }
}
