package net.silthus.skills;

import io.ebean.Database;
import kr.entree.spigradle.annotations.PluginMain;
import lombok.Getter;
import net.silthus.ebean.Config;
import net.silthus.ebean.EbeanWrapper;
import net.silthus.skills.listener.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.nio.file.Path;

@PluginMain
public class SkillsPlugin extends JavaPlugin {

    private SkillManager skillManager;
    private SkillPluginConfig config;
    private PlayerListener playerListener;

    public SkillsPlugin() {
    }

    public SkillsPlugin(
            JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void onEnable() {

        config = new SkillPluginConfig(Path.of("config.yml"));
        config.loadAndSave();

        this.skillManager = new SkillManager(this, connectToDatabase(), config);
        skillManager.registerDefaults();

        skillManager.load();

        this.playerListener = new PlayerListener(skillManager);
        Bukkit.getPluginManager().registerEvents(playerListener, this);
    }

    private Database connectToDatabase() {

        Config dbConfig = Config.builder()
                .entities(

                )
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
