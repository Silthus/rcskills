package net.silthus.skills;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import com.google.common.base.Strings;
import io.ebean.Database;
import kr.entree.spigradle.annotations.PluginMain;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.silthus.ebean.Config;
import net.silthus.ebean.EbeanWrapper;
import net.silthus.skills.commands.AdminCommands;
import net.silthus.skills.commands.SkillsCommand;
import net.silthus.skills.entities.ConfiguredSkill;
import net.silthus.skills.entities.PlayerSkill;
import net.silthus.skills.entities.SkilledPlayer;
import net.silthus.skills.listener.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.util.Optional;

@PluginMain
public class SkillsPlugin extends JavaPlugin {

    @Getter
    @Accessors(fluent = true)
    private static SkillsPlugin instance;

    @Getter
    private SkillManager skillManager;
    private Database database;
    private SkillPluginConfig config;
    private PlayerListener playerListener;
    private PaperCommandManager commandManager;

    private boolean testing = false;

    public SkillsPlugin() {
        instance = this;
    }

    public SkillsPlugin(
            JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
        instance = this;
        testing = true;
    }

    @Override
    public void onEnable() {

        loadConfig();
        setupDatabase();
        setupSkillManager();
        if (!testing) {
            setupListener();
            setupCommands();
        }
    }

    private void loadConfig() {

        getDataFolder().mkdirs();
        config = new SkillPluginConfig(new File(getDataFolder(), "config.yml").toPath());
        config.loadAndSave();
    }

    private void setupSkillManager() {

        this.skillManager = new SkillManager(this, config);
        skillManager.registerDefaults();

        skillManager.load();
    }

    private void setupListener() {

        this.playerListener = new PlayerListener(skillManager);
        Bukkit.getPluginManager().registerEvents(playerListener, this);
    }

    private void setupCommands() {

        this.commandManager = new PaperCommandManager(this);
        registerSkilledPlayerContext(commandManager);
        registerSkillContext(commandManager);

        commandManager.getCommandCompletions().registerAsyncCompletion("skills", context -> skillManager.loadedSkills().keySet());

        commandManager.registerCommand(new SkillsCommand(this));
        commandManager.registerCommand(new AdminCommands(getSkillManager()));
    }

    private void registerSkillContext(PaperCommandManager commandManager) {

        commandManager.getCommandContexts().registerContext(ConfiguredSkill.class, context -> {
            String skillName = context.popFirstArg();
            Optional<ConfiguredSkill> skill = ConfiguredSkill.findByAliasOrName(skillName);
            if (skill.isEmpty()) {
                throw new InvalidCommandArgument("Der Skill " + skillName + " wurde nicht gefunden.");
            }
            return skill.get();
        });
    }

    private void registerSkilledPlayerContext(PaperCommandManager commandManager) {

        commandManager.getCommandContexts().registerContext(SkilledPlayer.class, context -> {
            String playerName = context.popFirstArg();
            if (Strings.isNullOrEmpty(playerName)) {
                return skillManager.getPlayer(context.getPlayer());
            }
            Player player = Bukkit.getPlayerExact(playerName);
            if (player == null) {
                throw new InvalidCommandArgument("Der Spieler " + playerName + " wurde nicht gefunden.");
            }
            return skillManager.getPlayer(player);
        });
    }

    private void setupDatabase() {

        this.database = new EbeanWrapper(Config.builder(this)
                .entities(
                        ConfiguredSkill.class,
                        PlayerSkill.class,
                        SkilledPlayer.class
                )
                .build()).connect();
    }
}
