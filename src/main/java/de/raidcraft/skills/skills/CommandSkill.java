package de.raidcraft.skills.skills;

import de.raidcraft.skills.*;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@SkillInfo("command")
public class CommandSkill extends AbstractSkill implements Executable {

    public static class Factory implements SkillFactory<CommandSkill> {

        @Override
        public Class<CommandSkill> getSkillClass() {

            return CommandSkill.class;
        }

        @Override
        public CommandSkill create(SkillContext context) {

            return new CommandSkill(context, RCSkills.instance());
        }

    }
    private final RCSkills plugin;

    private final List<String> applyCommands = new ArrayList<>();
    private final List<String> removeCommands = new ArrayList<>();
    private final List<String> executeCommands = new ArrayList<>();
    private boolean asServer = false;

    public CommandSkill(SkillContext context, RCSkills plugin) {

        super(context);
        this.plugin = plugin;
    }

    @Override
    public void load(ConfigurationSection config) {

        this.applyCommands.clear();
        this.removeCommands.clear();
        this.executeCommands.clear();
        this.applyCommands.addAll(config.getStringList("apply"));
        this.applyCommands.addAll(config.getStringList("remove"));
        this.applyCommands.addAll(config.getStringList("execute"));
        this.asServer = config.getBoolean("server", false);
    }

    @Override
    public void apply() {

        context().player().ifPresent(player -> executeCommands(player, applyCommands));
    }

    @Override
    public void remove() {

        context().player().ifPresent(player -> executeCommands(player, removeCommands));
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) throws Exception {

        context().player().ifPresent(player -> executeCommands(context.player(), removeCommands));

        return context.success();
    }

    private void executeCommands(Player player, List<String> commands) {

        for (String cmd : commands) {
            cmd = PlaceholderAPI.setPlaceholders(player, cmd);
            Bukkit.getServer().dispatchCommand(asServer ? Bukkit.getConsoleSender() : player, cmd);
        }
    }
}
