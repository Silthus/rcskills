package net.silthus.skills;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import com.google.common.base.Strings;
import de.slikey.effectlib.EffectManager;
import io.ebean.Database;
import kr.entree.spigradle.annotations.PluginMain;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.silthus.ebean.Config;
import net.silthus.ebean.EbeanWrapper;
import net.silthus.skills.commands.AdminCommands;
import net.silthus.skills.commands.SkillsCommand;
import net.silthus.skills.entities.ConfiguredSkill;
import net.silthus.skills.entities.PlayerLevel;
import net.silthus.skills.entities.PlayerSkill;
import net.silthus.skills.entities.SkilledPlayer;
import net.silthus.skills.listener.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.codehaus.commons.compiler.CompileException;

import java.io.File;
import java.util.Optional;
import java.util.stream.Collectors;

@PluginMain
public class SkillsPlugin extends JavaPlugin {

    @Getter
    @Accessors(fluent = true)
    private static SkillsPlugin instance;

    @Getter
    private SkillManager skillManager;
    @Getter
    private LevelManager levelManager;
    private Database database;
    @Getter
    private SkillPluginConfig pluginConfig;
    private PlayerListener playerListener;
    private PaperCommandManager commandManager;
    @Getter
    private EffectManager effectManager;

    @Getter
    private static boolean testing = false;

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
        setupLevelManager();
        setupEffectManager();
        if (!testing) {
            setupListener();
            setupCommands();
        }
    }

    public void reload() {

        try {
            if (pluginConfig == null) {
                loadConfig();
            } else {
                getPluginConfig().load();
            }
            getSkillManager().reload();
            getLevelManager().load();
        } catch (CompileException e) {
            getLogger().severe("failed to parse level expression");
            e.printStackTrace();
        }
    }

    private void loadConfig() {

        getDataFolder().mkdirs();
        pluginConfig = new SkillPluginConfig(new File(getDataFolder(), "config.yml").toPath());
        pluginConfig.loadAndSave();
    }

    private void setupSkillManager() {

        this.skillManager = new SkillManager(this, pluginConfig);
        skillManager.registerDefaults();

        skillManager.load();
    }

    private void setupEffectManager() {

        this.effectManager = new EffectManager(this);
    }

    private void setupLevelManager() {

        try {
            this.levelManager = new LevelManager(getPluginConfig().getLevelConfig());
            levelManager.load();
            Bukkit.getPluginManager().registerEvents(levelManager, this);
        } catch (CompileException e) {
            getLogger().severe("failed to parse level expression");
            e.printStackTrace();
        }
    }

    private void setupListener() {

        this.playerListener = new PlayerListener(skillManager);
        Bukkit.getPluginManager().registerEvents(playerListener, this);
    }

    private void setupCommands() {

        this.commandManager = new PaperCommandManager(this);
        registerSkilledPlayerContext(commandManager);
        registerSkillContext(commandManager);

        commandManager.getCommandCompletions().registerAsyncCompletion("skills", context -> ConfiguredSkill.find
                .query().findSet().stream()
                .map(ConfiguredSkill::alias)
                .collect(Collectors.toSet()));

        commandManager.registerCommand(new SkillsCommand(this));
        commandManager.registerCommand(new AdminCommands(this));
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

        commandManager.getCommandContexts().registerIssuerAwareContext(SkilledPlayer.class, context -> {
            String playerName = context.popFirstArg();
            if (Strings.isNullOrEmpty(playerName)) {
                return SkilledPlayer.getOrCreate(context.getPlayer());
            }
            Player player = Bukkit.getPlayerExact(playerName);
            if (player == null) {
                throw new InvalidCommandArgument("Der Spieler " + playerName + " wurde nicht gefunden.");
            }
            return SkilledPlayer.getOrCreate(player);
        });
    }

    private void setupDatabase() {

        this.database = new EbeanWrapper(Config.builder(this)
                .entities(
                        ConfiguredSkill.class,
                        PlayerSkill.class,
                        SkilledPlayer.class,
                        PlayerLevel.class
                )
                .build()).connect();
    }
}
