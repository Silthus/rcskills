package de.raidcraft.skills;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import com.google.common.base.Strings;
import de.raidcraft.skills.commands.AdminCommands;
import de.raidcraft.skills.commands.PlayerCommands;
import de.raidcraft.skills.entities.*;
import de.raidcraft.skills.listener.BindingListener;
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
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.codehaus.commons.compiler.CompileException;

import java.io.File;
import java.util.Arrays;
import java.util.List;
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
    @Getter
    private BindingListener bindingListener;
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

        this.bindingListener = new BindingListener();
        Bukkit.getPluginManager().registerEvents(bindingListener, this);
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
        registerBindActionContext(commandManager);
        registerMaterialContext(commandManager);

        registerOthersCondition(commandManager);
        registerUnlockedCondition(commandManager);
        registerActiveCondition(commandManager);
        registerExecutableCondition(commandManager);

        registerSkillsCompletion(commandManager);
        registerUnlockedSkillsCompletion(commandManager);
        registerActiveSkillsCompletion(commandManager);
        registerBindActions(commandManager);
        registerMaterialCompletion(commandManager);

        commandManager.registerCommand(new PlayerCommands(this));
        commandManager.registerCommand(new AdminCommands(this));
    }

    private void registerSkillsCompletion(PaperCommandManager commandManager) {

        commandManager.getCommandCompletions().registerAsyncCompletion("skills", context -> ConfiguredSkill.find
                .query().findSet().stream()
                .filter(ConfiguredSkill::enabled)
                .filter(skill -> !skill.isChild())
                .map(ConfiguredSkill::alias)
                .collect(Collectors.toSet()));
    }

    private void registerUnlockedSkillsCompletion(PaperCommandManager commandManager) {

        commandManager.getCommandCompletions().registerAsyncCompletion("unlocked-skills", context -> PlayerSkill.find
                .query().where()
                .eq("player_id", context.getPlayer().getUniqueId())
                .and().eq("status", SkillStatus.UNLOCKED.getValue())
                .and().isNull("parent")
                .findSet().stream()
                .filter(skill -> skill.configuredSkill().enabled())
                .map(PlayerSkill::alias)
                .collect(Collectors.toSet())
        );
    }

    private void registerActiveSkillsCompletion(PaperCommandManager commandManager) {

        commandManager.getCommandCompletions().registerAsyncCompletion("executable-skills", context -> PlayerSkill.find.query().where()
                .eq("status", SkillStatus.ACTIVE.getValue())
                .and().isNull("parent")
                .findSet().stream()
                .filter(skill -> getSkillManager().isExecutable(skill.configuredSkill()))
                .map(PlayerSkill::alias)
                .collect(Collectors.toSet()));
    }

    private void registerMaterialCompletion(PaperCommandManager commandManager) {

        commandManager.getCommandCompletions().registerAsyncCompletion("materials", context ->
                Arrays.stream(Material.values())
                .map(Material::getKey)
                .map(NamespacedKey::getKey)
                .collect(Collectors.toSet())
        );
    }

    private void registerBindActions(PaperCommandManager commandManager) {

        commandManager.getCommandCompletions().registerAsyncCompletion("bind-actions", context -> Arrays.
                stream(ItemBinding.Action.values())
                .map(Enum::name)
                .collect(Collectors.toSet())
        );
    }

    private void registerBindActionContext(PaperCommandManager commandManager) {

        commandManager.getCommandContexts().registerContext(ItemBinding.Action.class,
                context -> {
                    String name = context.popFirstArg();
                    try {
                        return ItemBinding.Action.valueOf(name);
                    } catch (IllegalArgumentException e) {
                        throw new InvalidCommandArgument("Es gibt keine Bind Action mit dem Namen " + name);
                    }
                });
    }

    private void registerMaterialContext(PaperCommandManager commandManager) {

        commandManager.getCommandContexts().registerContext(Material.class, context -> Material.matchMaterial(context.popFirstArg()));
    }

    private void registerSkillContext(PaperCommandManager commandManager) {

        commandManager.getCommandContexts().registerContext(ConfiguredSkill.class, context -> {
            String skillName = context.popFirstArg();
            try {
                UUID uuid = UUID.fromString(skillName);
                return ConfiguredSkill.find.byId(uuid);
            } catch (Exception e) {
                Optional<ConfiguredSkill> skill = ConfiguredSkill.findByAliasOrName(skillName)
                        .filter(skill1 -> !skill1.isChild());
                if (skill.isEmpty()) {
                    throw new InvalidCommandArgument("Der Skill " + skillName + " wurde nicht gefunden.");
                }
                return skill.get();
            }
        });
    }

    private void registerPlayerSkillContext(PaperCommandManager commandManager) {

        commandManager.getCommandContexts().registerContext(PlayerSkill.class, context -> {

            SkilledPlayer player = null;
            ConfiguredSkill skill;
            PlayerSkill playerSkill = null;
            String arg = context.popFirstArg();
            try {
                UUID id = UUID.fromString(arg);
                skill = ConfiguredSkill.find.byId(id);
                if (skill == null) {
                    playerSkill = PlayerSkill.find.byId(id);
                }
            } catch (Exception e) {
                skill = ConfiguredSkill.findByAliasOrName(arg).orElse(null);
            }

            if (playerSkill != null) return playerSkill;

            if (context.getPlayer() != null) {
                player = SkilledPlayer.getOrCreate(context.getPlayer());
            }

            if (skill == null) {
                throw new ConditionFailedException("Es konnte kein Skill mit einer ID oder Namen gefunden werden: " + String.join(",", context.getArgs()));
            }
            if (player == null) {
                throw new ConditionFailedException("Es konnte kein Spieler mit einer ID oder Namen gefunden werden: " + String.join(",", context.getArgs()));
            }
            if (skill.isChild()) {
                throw new ConditionFailedException("Du kannst keine Sub-Skills auswählen. Bitte nehme den Haupt Skill.");
            }

            return PlayerSkill.getOrCreate(player, skill);
        });
    }

    private void registerSkilledPlayerContext(PaperCommandManager commandManager) {

        commandManager.getCommandContexts().registerIssuerAwareContext(SkilledPlayer.class, context -> {

            if (context.hasFlag("self")) {
                return SkilledPlayer.getOrCreate(context.getPlayer());
            }

            String arg = context.popFirstArg();
            Player player;
            if (arg.startsWith("@")) {
                player = selectPlayer(context.getSender(), arg);
            } else {
                if (Strings.isNullOrEmpty(arg)) {
                    return SkilledPlayer.getOrCreate(context.getPlayer());
                }
                try {
                    UUID uuid = UUID.fromString(arg);
                    player = Bukkit.getPlayer(uuid);
                } catch (Exception e) {
                    player = Bukkit.getPlayerExact(arg);
                }
            }

            if (player == null) {
                throw new InvalidCommandArgument("Der Spieler " + arg + " wurde nicht gefunden.");
            }

            return SkilledPlayer.getOrCreate(player);
        });
    }

    private Player selectPlayer(CommandSender sender, String playerIdentifier) {

        List<Player> matchedPlayers;
        try {
            matchedPlayers = getServer().selectEntities(sender, playerIdentifier).parallelStream()
                    .unordered()
                    .filter(e -> e instanceof Player)
                    .map(e -> ((Player) e))
                    .collect(Collectors.toList());
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InvalidCommandArgument(String.format("Error parsing selector '%s' for %s! See console for more details",
                    playerIdentifier, sender.getName()));
        }
        if (matchedPlayers.isEmpty()) {
            throw new InvalidCommandArgument(String.format("No player found with selector '%s' for %s",
                    playerIdentifier, sender.getName()));
        }
        if (matchedPlayers.size() > 1) {
            throw new InvalidCommandArgument(String.format("Error parsing selector '%s' for %s. ambiguous result (more than one player matched) - %s",
                    playerIdentifier, sender.getName(), matchedPlayers.toString()));
        }

        return matchedPlayers.get(0);
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

    private void registerExecutableCondition(PaperCommandManager commandManager) {

        commandManager.getCommandConditions().addCondition(PlayerSkill.class, "executable", (context, execContext, value) -> {
            if (!value.executable()) {
                throw new ConditionFailedException("Der Skill " + value.name() + " kann nicht aktiv ausgeführt werden.");
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
                        SkillSlot.class,
                        ItemBinding.class
                )
                .build()).connect();
    }
}
