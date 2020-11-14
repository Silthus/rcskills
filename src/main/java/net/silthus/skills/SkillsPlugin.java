package net.silthus.skills;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import io.ebean.Database;
import kr.entree.spigradle.annotations.PluginMain;
import lombok.Getter;
import net.silthus.ebean.Config;
import net.silthus.ebean.EbeanWrapper;
import net.silthus.skills.commands.AdminCommands;
import net.silthus.skills.commands.SkillsCommand;
import net.silthus.skills.entities.PlayerSkill;
import net.silthus.skills.entities.SkilledPlayer;
import net.silthus.skills.listener.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

@PluginMain
public class SkillsPlugin extends JavaPlugin {

    @Getter
    private SkillManager skillManager;
    private SkillPluginConfig config;
    private PlayerListener playerListener;
    private PaperCommandManager commandManager;

    public SkillsPlugin() {
    }

    public SkillsPlugin(
            JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void onEnable() {

        loadConfig();
        setupSkillManager();
        setupListener();
        setupCommands();
    }

    private void loadConfig() {

        getDataFolder().mkdirs();
        config = new SkillPluginConfig(new File(getDataFolder(), "config.yml").toPath());
        config.loadAndSave();
    }

    private void setupSkillManager() {

        this.skillManager = new SkillManager(this, connectToDatabase(), config);
        skillManager.registerDefaults();

        skillManager.load();
    }

    private void setupListener() {

        this.playerListener = new PlayerListener(skillManager);
        Bukkit.getPluginManager().registerEvents(playerListener, this);
    }

    private void setupCommands() {

        this.commandManager = new PaperCommandManager(this);
        try {
            saveResource("lang_de.yml", false);
            commandManager.addSupportedLanguage(Locale.GERMAN);
            commandManager.getLocales().loadYamlLanguageFile("lang_de.yml", Locale.GERMAN);
            commandManager.getLocales().setDefaultLocale(Locale.GERMAN);
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().severe("unable to load locales");
            e.printStackTrace();
        }

        commandManager.getCommandCompletions().registerAsyncCompletion("skills", context -> skillManager.loadedSkills().keySet());
        commandManager.getCommandContexts().registerContext(Skill.class, context -> {
            Optional<ConfiguredSkill> skill = getSkillManager().findSkillByNameOrId(context.popFirstArg());
            if (skill.isEmpty()) {
                throw new InvalidCommandArgument("{@@rcskills.resolver.skill.error}");
            }
            return skill.get();
        });
        commandManager.getCommandContexts().registerContext(SkilledPlayer.class, context -> {
            Player player = Bukkit.getPlayerExact(context.popFirstArg());
            if (player == null) {
                throw new InvalidCommandArgument("{@@invalid-player}");
            }
            return skillManager.getPlayer(player);
        });

        commandManager.registerCommand(new SkillsCommand(this));
        commandManager.registerCommand(new AdminCommands(getSkillManager()));
    }

    private Database connectToDatabase() {

        Config dbConfig = Config.builder()
                .entities(SkilledPlayer.class, PlayerSkill.class)
                .driverPath(new File("lib"))
                .autoDownloadDriver(true)
                .migrations(getClass())
                .url(config.getDatabase().getUrl())
                .username(config.getDatabase().getUsername())
                .password(config.getDatabase().getPassword())
                .driver(config.getDatabase().getDriver())
                .build();
        return new EbeanWrapper(dbConfig).getDatabase();
    }
}
