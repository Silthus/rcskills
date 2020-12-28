package de.raidcraft.skills;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import com.google.common.base.Strings;
import de.raidcraft.skills.commands.AdminCommands;
import de.raidcraft.skills.commands.PlayerCommands;
import de.raidcraft.skills.entities.*;
import de.raidcraft.skills.listener.PlayerListener;
import de.slikey.effectlib.EffectManager;
import io.ebean.Database;
import kr.entree.spigradle.annotations.PluginMain;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.Config;
import net.silthus.ebean.EbeanWrapper;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.codehaus.commons.compiler.CompileException;

import java.io.File;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@PluginMain
public class SkillsPlugin extends JavaPlugin {

    public static final String PERMISSION_PREFIX = "rcskills.";
    public static final String SKILL_PERMISSION_PREFIX = PERMISSION_PREFIX + "skill.";
    public static final String BYPASS_ACTIVE_SKILL_LIMIT = PERMISSION_PREFIX + "slots.bypass";
    public static final String BYPASS_REQUIREMENT_CHECKS = PERMISSION_PREFIX + "requirements.bypass";

    @Getter
    @Accessors(fluent = true)
    private static SkillsPlugin instance;

    @Getter
    private SkillManager skillManager;
    @Getter
    private LevelManager levelManager;
    @Getter
    private SlotManager slotManager;
    @Getter
    private TargetManager targetManager;
    private Database database;
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private SkillPluginConfig pluginConfig;
    private Messages messages;
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
        setupSlotManager();
        setupTargetManager();
        setupEffectManager();
        setupListener();
        if (!isTesting()) {
            setupCommands();
            registerPermissions();
        }
    }

    @Override
    public void onDisable() {

        getSkillManager().unload();
    }

    private void registerPermissions() {

        Bukkit.getPluginManager().addPermission(new Permission(BYPASS_ACTIVE_SKILL_LIMIT, PermissionDefault.FALSE));
        Bukkit.getPluginManager().addPermission(new Permission(BYPASS_REQUIREMENT_CHECKS, PermissionDefault.FALSE));
    }

    public void reload() {

        try {
            loadConfig();
            getSkillManager().reload();
            getLevelManager().load();
            getSlotManager().load(getPluginConfig().getSlotConfig());
        } catch (CompileException e) {
            getLogger().severe("failed to parse level expression");
            e.printStackTrace();
        }
    }

    private void loadConfig() {

        try {
            getDataFolder().mkdirs();
            pluginConfig = new SkillPluginConfig(new File(getDataFolder(), "config.yml").toPath());
            pluginConfig.loadAndSave();
            saveResource("messages.yml", false);
            messages = new Messages(new File(getDataFolder(), "messages.yml"));
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void setupSkillManager() {

        this.skillManager = new SkillManager(this, pluginConfig);
        skillManager.registerDefaults();

        // delay the loading of skills by one tick to wait for all plugins to load
        // this is needed to properly auto detect all skill factories inside the other plugins
        Bukkit.getScheduler().runTaskLater(this, () -> skillManager.load(), 1L);
    }

    private void setupEffectManager() {

        this.effectManager = new EffectManager(this);
    }

    private void setupTargetManager() {

        this.targetManager = new TargetManager(this);
        targetManager.load();
    }

    private void setupLevelManager() {

        try {
            this.levelManager = new LevelManager(this);
            levelManager.load();
            Bukkit.getPluginManager().registerEvents(levelManager, this);
        } catch (CompileException e) {
            getLogger().severe("failed to parse level expression: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupSlotManager() {

        try {
            this.slotManager = new SlotManager();
            slotManager.load(getPluginConfig().getSlotConfig());
        } catch (CompileException e) {
            getLogger().severe("failed to parse slot price expression: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupListener() {

        this.playerListener = new PlayerListener(skillManager);
        Bukkit.getPluginManager().registerEvents(playerListener, this);
    }

    void setupCommands() {

        this.commandManager = new PaperCommandManager(this);
        commandManager.enableUnstableAPI("help");
        if (isTesting()) {
            commandManager.setDefaultExceptionHandler((command, registeredCommand, sender, args, t) -> {
                t.printStackTrace();
                return true;
            });
        }

        registerSkilledPlayerContext(commandManager);
        registerSkillContext(commandManager);
        registerPlayerSkillContext(commandManager);

        registerOthersCondition(commandManager);
        registerUnlockedCondition(commandManager);
        registerActiveCondition(commandManager);

        commandManager.getCommandCompletions().registerAsyncCompletion("skills", context -> ConfiguredSkill.find
                .query().findSet().stream()
                .filter(ConfiguredSkill::enabled)
                .map(ConfiguredSkill::alias)
                .collect(Collectors.toSet()));

        commandManager.getCommandCompletions().registerAsyncCompletion("unlocked-skills", context -> PlayerSkill.find
                .query().where()
                .eq("player_id", context.getPlayer().getUniqueId())
                .and().eq("status", SkillStatus.UNLOCKED.getValue())
                .findSet().stream()
                .filter(skill -> skill.configuredSkill().enabled())
                .map(PlayerSkill::alias)
                .collect(Collectors.toSet())
        );

        commandManager.registerCommand(new PlayerCommands(this));
        commandManager.registerCommand(new AdminCommands(this));
    }

    private void registerSkillContext(PaperCommandManager commandManager) {

        commandManager.getCommandContexts().registerContext(ConfiguredSkill.class, context -> {
            String skillName = context.popFirstArg();
            try {
                UUID uuid = UUID.fromString(skillName);
                return ConfiguredSkill.find.byId(uuid);
            } catch (Exception e) {
                Optional<ConfiguredSkill> skill = ConfiguredSkill.findByAliasOrName(skillName);
                if (skill.isEmpty()) {
                    throw new InvalidCommandArgument("Der Skill " + skillName + " wurde nicht gefunden.");
                }
                return skill.get();
            }
        });
    }

    private void registerPlayerSkillContext(PaperCommandManager commandManager) {

        commandManager.getCommandContexts().registerIssuerAwareContext(PlayerSkill.class, context -> {
            SkilledPlayer player = null;
            ConfiguredSkill skill = null;
            PlayerSkill playerSkill = null;
            for (String arg : context.getArgs()) {
                try {
                    UUID id = UUID.fromString(arg);
                    if (player == null)
                        player = SkilledPlayer.find.byId(id);
                    if (skill == null)
                        skill = ConfiguredSkill.find.byId(id);
                    if (playerSkill == null)
                        playerSkill = PlayerSkill.find.byId(id);
                } catch (Exception ignored) {
                    if (player == null)
                        player = SkilledPlayer.find.query().where().eq("name", arg).findOne();
                    if (skill == null)
                        skill = ConfiguredSkill.findByAliasOrName(arg).orElse(null);
                }
            }

            if (playerSkill != null) {
                return playerSkill;
            }

            if (player == null && context.getPlayer() != null) {
                player = SkilledPlayer.getOrCreate(context.getPlayer());
            }

            if (skill == null) {
                throw new ConditionFailedException("Es konnte kein Skill mit einer ID oder Namen gefunden werden: " + String.join(",", context.getArgs()));
            }
            if (player == null) {
                throw new ConditionFailedException("Es konnte kein Spieler mit einer ID oder Namen gefunden werden: " + String.join(",", context.getArgs()));
            }
            return PlayerSkill.getOrCreate(player, skill);
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

    private void registerOthersCondition(PaperCommandManager commandManager) {

        commandManager.getCommandConditions().addCondition(SkilledPlayer.class, "others", (context, execContext, value) -> {
            if (context.getIssuer().hasPermission(PERMISSION_PREFIX + context.getConfigValue("perm", "cmd") + ".others")) {
                return;
            }
            if (context.getIssuer().getUniqueId().equals(value.id())) {
                return;
            }
            throw new ConditionFailedException("Du hast nicht genügend Rechte um Befehle im Namen von anderen Spielern auszuführen.");
        });
    }

    private void registerUnlockedCondition(PaperCommandManager commandManager) {

        commandManager.getCommandConditions().addCondition(PlayerSkill.class, "unlocked", (context, execContext, value) -> {
            if (!value.unlocked()) {
                throw new ConditionFailedException("Du hast den Skill " + value.name() + " noch nicht freigeschaltet.");
            }
        });
    }

    private void registerActiveCondition(PaperCommandManager commandManager) {

        commandManager.getCommandConditions().addCondition(PlayerSkill.class, "active", (context, execContext, value) -> {
            if (!value.active()) {
                throw new ConditionFailedException("Der Skill " + value.name() + " ist nicht aktiv.");
            }
        });
    }

    private void setupDatabase() {

        this.database = new EbeanWrapper(Config.builder(this)
                .entities(
                        ConfiguredSkill.class,
                        PlayerSkill.class,
                        SkilledPlayer.class,
                        Level.class,
                        LevelHistory.class,
                        DataStore.class,
                        SkillSlot.class
                )
                .build()).connect();
    }
}
