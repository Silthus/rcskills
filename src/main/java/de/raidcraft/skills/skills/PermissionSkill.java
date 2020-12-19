package de.raidcraft.skills.skills;

import de.raidcraft.skills.*;
import de.raidcraft.skills.entities.PlayerSkill;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.PermissionAttachment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SkillInfo("permission")
public class PermissionSkill extends AbstractSkill {

    public static class PermissionSkillFactory implements SkillFactory<PermissionSkill> {

        @Override
        public Class<PermissionSkill> getSkillClass() {

            return PermissionSkill.class;
        }

        @Override
        public PermissionSkill create(SkillContext context) {

            return new PermissionSkill(context, SkillsPlugin.instance());
        }
    }

    private final SkillsPlugin plugin;
    private final List<String> permissions = new ArrayList<>();
    private final Set<PermissionAttachment> attachments = new HashSet<>();

    public PermissionSkill(SkillContext context, SkillsPlugin plugin) {

        super(context);
        this.plugin = plugin;
    }

    @Override
    public void load(ConfigurationSection config) {

        this.permissions.clear();
        this.permissions.addAll(config.getStringList("permissions"));
    }

    @Override
    public void apply() {

        context().player().ifPresent(player -> attachments
                .addAll(permissions.stream()
                        .map(permission -> player.addAttachment(plugin, permission, true))
                        .collect(Collectors.toList())));
    }

    @Override
    public void remove() {

        context().player().ifPresent(player -> attachments.forEach(player::removeAttachment));
        attachments.clear();
    }
}
