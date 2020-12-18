package de.raidcraft.skills.skills;

import de.raidcraft.skills.Skill;
import de.raidcraft.skills.SkillInfo;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.entities.SkilledPlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.PermissionAttachment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@SkillInfo("permission")
public class PermissionSkill implements Skill {

    private final SkillsPlugin plugin;
    private final List<String> permissions = new ArrayList<>();
    private final Map<UUID, List<PermissionAttachment>> attachments = new HashMap<>();

    public PermissionSkill(SkillsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void load(ConfigurationSection config) {

        this.permissions.clear();
        this.permissions.addAll(config.getStringList("permissions"));
    }

    @Override
    public void apply(SkilledPlayer player) {

        List<PermissionAttachment> attachments = this.attachments.getOrDefault(player.id(), new ArrayList<>());
        attachments
                .addAll(permissions.stream()
                        .map(permission -> player.getBukkitPlayer()
                                .map(p -> p.addAttachment(plugin, permission, true)).orElse(null))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
        this.attachments.put(player.id(), attachments);
    }

    @Override
    public void remove(SkilledPlayer player) {

        attachments.getOrDefault(player.id(), new ArrayList<>())
                .forEach(permissionAttachment -> player.getBukkitPlayer().ifPresent(p -> p.removeAttachment(permissionAttachment)));
    }
}
