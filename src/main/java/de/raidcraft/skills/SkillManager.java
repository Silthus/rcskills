package de.raidcraft.skills;

import com.google.common.base.Strings;
import de.raidcraft.skills.entities.ConfiguredSkill;
import de.raidcraft.skills.entities.PlayerSkill;
import de.raidcraft.skills.entities.SkilledPlayer;
import de.raidcraft.skills.requirements.*;
import de.raidcraft.skills.skills.EmptySkill;
import de.raidcraft.skills.skills.PermissionSkill;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skills.util.JarUtil;
import io.ebean.annotation.Transactional;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import net.silthus.configmapper.ConfigurationException;
import net.silthus.configmapper.bukkit.BukkitConfigMap;
import net.silthus.ebean.BaseEntity;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Log(topic = "RCSkills")
@Getter
@Accessors(fluent = true)
public final class SkillManager {

    public static final String SUB_SKILL_SECTION = "skills";

    private final Map<String, Requirement.Registration<?>> requirements = new HashMap<>();
    private final Map<String, Skill.Registration<?>> skillTypes = new HashMap<>();

    // player_id -> player_skill_id -> context
    private final Map<UUID, Map<UUID, SkillContext>> cachedPlayerSkills = new HashMap<>();

    private final RCSkills plugin;
    private final SkillPluginConfig config;

    public SkillManager(RCSkills plugin, SkillPluginConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    /**
     * Registers default requirements and skill types provided by this plugin.
     */
    public void registerDefaults() {

        registerRequirement(PermissionRequirement.class, PermissionRequirement::new);
        registerRequirement(SkillRequirement.class, SkillRequirement::new);
        registerRequirement(LevelRequirement.class, LevelRequirement::new);
        registerRequirement(MoneyRequirement.class, MoneyRequirement::new);
        registerRequirement(SkillPointRequirement.class, SkillPointRequirement::new);

        registerSkill(new PermissionSkill.PermissionSkillFactory());
        registerSkill(new EmptySkill.Factory());
    }

    public void reload() {

        skillTypes.clear();
        requirements.clear();
        registerDefaults();

        load();

        reloadPlayerSkills();
    }

    @Transactional
    public void load() {

        loadSkillsFromPlugins();
        loadSkillsFromModules();
        List<ConfiguredSkill> loadedSkills = loadSkills(new File(plugin.getDataFolder(), config.getSkillsPath()).toPath());

        ConfiguredSkill.find.query()
                .where().eq("enabled", true)
                .where().isNull("parent")
                .where().notIn("id", loadedSkills.stream().map(BaseEntity::id).collect(Collectors.toUnmodifiableList()))
                .findList()
                .forEach(skill -> {
                    skill.enabled(false).save();
                    log.warning("disabled \"" + skill.alias() + "\" in the database because it was not loaded from disk.");
                });

        ConfiguredSkill.find.query()
                .where().eq("enabled", false)
                .where().isNull("parent")
                .where().in("id", loadedSkills.stream().map(BaseEntity::id).collect(Collectors.toUnmodifiableList()))
                .findList()
                .forEach(skill -> {
                    boolean enabled = loadedSkills.stream()
                            .filter(s -> s.id().equals(skill.id()))
                            .findFirst()
                            .map(ConfiguredSkill::enabled)
                            .orElse(true);
                    skill.enabled(enabled).save();
                    if (enabled)
                        log.info("enabled previous disabled skill \"" + skill.alias() + "\" in the database because it was loaded again from disk.");
                });

        getSkillPermissions(loadedSkills).forEach(permission -> {
            Bukkit.getPluginManager().removePermission(permission);
            Bukkit.getPluginManager().addPermission(permission);
        });
    }

    public void unload() {

        clearCache();
        skillTypes.clear();
        requirements.clear();
    }

    List<Permission> getSkillPermissions(List<ConfiguredSkill> skills) {

        return skills.stream()
                .filter(ConfiguredSkill::enabled)
                .map(skill -> new Permission(
                        RCSkills.SKILL_PERMISSION_PREFIX + skill.alias(),
                        skill.description(),
                        skill.restricted() ? PermissionDefault.OP : PermissionDefault.TRUE)
                ).collect(Collectors.toUnmodifiableList());
    }

    void loadSkillsFromPlugins() {

        if (RCSkills.isTesting() || !plugin.getPluginConfig().isLoadClassesFromPlugins()) return;

        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {

            if (plugin.equals(plugin())) continue;

            try {
                loadClassesFromJar(new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()));
            } catch (URISyntaxException e) {
                log.severe("unable to find valid jar file location of plugin " + plugin.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    void loadSkillsFromModules() {

        File modules = new File(plugin.getDataFolder(), config.getModulePath());
        modules.mkdirs();
        Path path = modules.toPath();
        try {
            Files.walk(path, Integer.MAX_VALUE)
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .filter(file -> file.getName().endsWith(".jar"))
                    .forEach(this::loadClassesFromJar);
        } catch (IOException e) {
            log.severe("unable to load modules from " + path + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadClassesFromJar(File jarFile) {
        JarUtil.findClasses(plugin.getClass().getClassLoader(), jarFile, SkillFactory.class::isAssignableFrom).stream()
                .map(aClass -> {
                    try {
                        return aClass.getDeclaredConstructor();
                    } catch (NoSuchMethodException e) {
                        log.warning("unable to find a public no arguments constructor for skill factory " + aClass.getCanonicalName() + ": " + e.getMessage());
                        log.warning("make sure you register your factory or skill manually with the SkillManager#registerSkill(...) method!");
                        return null;
                    }
                }).filter(Objects::nonNull)
                .map(constructor -> {
                    try {
                        return (SkillFactory<?>) constructor.newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        log.severe("unable to create a new instance of the skill factory "
                                + constructor.getClass().getCanonicalName() + " --> " + constructor.getName() + ": " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    }
                }).filter(Objects::nonNull)
                .forEach(this::registerSkill);
    }

    /**
     * Recursively loads all skill configs in the given path, creates and caches new skill instances for them.
     * Each file will be loaded and passed into {@link #loadSkill(String, ConfigurationSection)}.
     *
     * @param path the path to the skill configs
     */
    List<ConfiguredSkill> loadSkills(Path path) {

        try {
            Files.createDirectories(path);
            List<File> files = Files.find(path, Integer.MAX_VALUE,
                    (file, fileAttr) -> fileAttr.isRegularFile())
                    .map(Path::toFile).collect(Collectors.toList());


            int fileCount = files.size();
            List<ConfiguredSkill> skills = files.stream()
                    .map(file -> loadSkill(path, file))
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList());

            log.info("Loaded " + skills.size() + "/" + fileCount + " skills from " + path);
            return skills;
        } catch (IOException e) {
            log.severe("unable to load skills from " + path + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Tries to load a skill from the given file configuration.
     * <p>The load operation will fail if the file does not exist or
     * the type key inside the config does not match any registered skill type.
     * <p>If the config does not defined an id the unique path name of the file will be used as id.
     * <p>The skill will be cached if the loading succeeds.
     * <p>An empty optional will be returned in all error cases.
     *
     * @param file the file to load the skill from
     * @return the loaded skill or an empty optional.
     */
    Optional<ConfiguredSkill> loadSkill(Path base, @NonNull File file) {

        if (!file.exists() || !(file.getName().endsWith(".yml") && !file.getName().endsWith(".yaml"))) {
            return Optional.empty();
        }

        try {
            YamlConfiguration config = new YamlConfiguration();
            config.load(file);
            Optional<ConfiguredSkill> skill = loadSkill(ConfigUtil.getFileIdentifier(base, file), config);
            config.save(file);
            skill.ifPresentOrElse(s -> {
                log.info("loaded skill \"" + s.alias() + "\" (" + s.type() + ") from: " + file);
            }, () -> log.warning("failed to load skill from config: " + file));
            return skill;
        } catch (IOException | InvalidConfigurationException e) {
            log.severe("unable to load skill config " + file.getAbsolutePath() + ": " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Registers a new requirement type with this skill manager.
     * <p>Requirement types are used to instantiate and load {@link Requirement}s as they are needed.
     * <p>Make sure your requirement is tagged with a @{@link RequirementInfo} annotation and has a unique type identifier.
     *
     * @param requirementClass the class of the requirement type
     * @param supplier the supplier that can create the requirement
     * @param <TRequirement> the type of the requirement
     * @return this skill manager
     */
    public <TRequirement extends Requirement> SkillManager registerRequirement(Class<TRequirement> requirementClass, Supplier<TRequirement> supplier) {

        if (!requirementClass.isAnnotationPresent(RequirementInfo.class)) {
            log.severe("Cannot register requirement " + requirementClass.getCanonicalName() + " without a @RequirementType annotation.");
            return this;
        }

        String type = requirementClass.getAnnotation(RequirementInfo.class).value().toLowerCase();
        if (requirements().containsKey(type)) {
            log.severe("Cannot register requirement: " + requirementClass.getCanonicalName()
                    + "! A requirement with the same type identifier '"
                    + type + "' is already registered: "
                    + requirements.get(type).requirementClass().getCanonicalName());
            return this;
        }

        requirements.put(type, new Requirement.Registration<>(type, requirementClass, supplier));
        log.info("registered requirement type: " + type + " [" + requirementClass.getCanonicalName() + "]");
        return this;
    }

    /**
     * Unregisters the given requirement type.
     * <p>Loaded instances of the requirement will still remain, but no new instances can be created.
     * <p>Use this method to cleanup when your plugin shuts down.
     *
     * @param requirement the class of the requirement to remove
     * @return this skill manager
     */
    public SkillManager unregisterRequirement(Class<? extends Requirement> requirement) {

        requirements.values().stream().filter(registration -> registration.requirementClass().equals(requirement))
                .forEach(registration -> requirements.remove(registration.identifier()));
        return this;
    }

    /**
     * Registers the given skill factory with this skill manager.
     *
     * @param factory the factory to register
     * @param <TSkill> the type of the skill
     */
    public <TSkill extends Skill> void registerSkill(SkillFactory<TSkill> factory) {

        registerSkill(factory.getSkillClass(), factory::create);
    }

    /**
     * Registers the given skill type with this skill manager.
     * <p>Make sure your skill class is annotated with @{@link SkillInfo} or the registration will fail.
     * <p>The provided supplier will be used to create instances of the given skill type which are then loaded
     * and applied to players.
     *
     * @param skillClass the class of the skill type to register
     * @param supplier the supplier that can create new instances of the skill
     * @param <TSkill> the type of the skill
     */
    public <TSkill extends Skill> void registerSkill(Class<TSkill> skillClass, Function<SkillContext, TSkill> supplier) {

        if (skillTypes.values().stream().anyMatch(registration -> registration.skillClass().equals(skillClass))) {
            return;
        }

        if (!skillClass.isAnnotationPresent(SkillInfo.class)) {
            log.severe("Cannot register skill " + skillClass.getCanonicalName() + " without a @SkillInfo annotation.");
            return;
        }

        SkillInfo info = skillClass.getAnnotation(SkillInfo.class);
        String type = info.value().toLowerCase();
        if (skillTypes.containsKey(type)) {
            log.severe("Cannot register skill: " + skillClass.getCanonicalName()
                    + "! A skill with the same type identifier '"
                    + type + "' is already registered: "
                    + skillTypes.get(type).skillClass().getCanonicalName());
            return;
        }

        for (String depend : info.depends()) {
            if (Bukkit.getPluginManager().getPlugin(depend) == null) {
                log.severe("Cannot register skill " + skillClass.getCanonicalName() + " (" + type + ")! Missing plugin dependency of " + depend);
                return;
            }
        }

        skillTypes.put(type, new Skill.Registration<>(skillClass, info, supplier));
        log.info("registered skill type: " + type + " [" + skillClass.getCanonicalName() + "]");
    }

    /**
     * Unregisters the given skill type from this skill manager.
     * <p>Existing instances of the skill will not be removed, but new ones can not be created.
     * <p>Use this method to cleanup your skill types when the plugin shuts down.
     *
     * @param skill the class of the skill to unregister
     * @return this skill manager
     */
    public SkillManager unregisterSkill(Class<? extends Skill> skill) {

        if (!skill.isAnnotationPresent(SkillInfo.class)) {
            return this;
        }
        skillTypes.remove(skill.getAnnotation(SkillInfo.class).value().toLowerCase());
        return this;
    }

    /**
     * Loads and applies all active skills to the given player.
     * <p>Will do nothing if the player is already loaded.
     *
     * @param player the player that should be loaded
     */
    public void load(@NonNull Player player) {

        SkilledPlayer skilledPlayer = SkilledPlayer.getOrCreate(player);
        skilledPlayer.activeSkills().forEach(PlayerSkill::enable);

        ConfiguredSkill.autoUnlockableSkills(skilledPlayer.level().getLevel())
                .forEach(skilledPlayer::addSkill);

        skilledPlayer.unlockedSkills()
                .stream()
                .filter(skill -> !skill.isChild())
                .filter(skill -> skill.configuredSkill().autoUnlock())
                .forEach(PlayerSkill::activate);
    }

    /**
     * Unloads the player and all of his skills.
     * <p>Will do nothing if the player was never loaded.
     *
     * @param player the player to unload
     */
    public void unload(@NonNull Player player) {

        clearPlayerCache(player.getUniqueId());
    }

    private void reloadPlayerSkills() {

        Bukkit.getOnlinePlayers()
                .stream().map(SkilledPlayer::getOrCreate)
                .flatMap(player -> player.activeSkills().stream())
                .forEach(PlayerSkill::reload);
    }

    private void clearCache() {

        cachedPlayerSkills.keySet().stream().collect(Collectors.toUnmodifiableSet())
                .forEach(this::clearPlayerCache);
        cachedPlayerSkills.clear();
    }

    public void clearPlayerCache(UUID uuid) {

        Map<UUID, SkillContext> cache = cachedPlayerSkills.remove(uuid);
        if (cache != null) {
            cache.values().forEach(SkillContext::disable);
            cache.clear();
        }
    }

    /**
     * Loads and creates requirements from the provided configuration section.
     * <p>The method expects a section with unique keys and each section
     * must at least contain a valid {@code type: <requirement_type>}.
     * <p><pre>{@code
     *   requirements:
     *     foo:
     *       type: permission
     *       permissions: ...
     *     bar:
     *       type: skill
     *       skill: foobar
     *
     *   SkillManager.instance().loadRequirements(config.getConfigurationSection("requirements"));
     * }</pre>
     *
     * @param config the config section to load the requirements from
     *               can be null or empty
     * @return a list of loaded requirements
     */
    public List<Requirement> loadRequirements(ConfigurationSection config) {

        if (config == null) return new ArrayList<>();
        if (config.getKeys(false).isEmpty()) return new ArrayList<>();

        ArrayList<Requirement> requirements = new ArrayList<>();

        for (String requirementKey : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(requirementKey);
            if (section == null) continue;
            String type = section.getString("type", requirementKey);
            if (requirements().containsKey(type)) {
                Requirement requirement = requirements().get(type).supplier().get();
                try {
                    BukkitConfigMap.of(requirement).with(section).applyTo(requirement);
                } catch (ConfigurationException e) {
                    log.severe("Failed to apply config to requirement " + requirementKey + ": " + e.getMessage());
                }
                requirements.add(requirement.load(section));
            } else {
                log.warning("unable to find the requirement type " + type + " for " + requirementKey + " in " + config.getName() + "." + requirementKey);
            }
        }

        return requirements;
    }

    /**
     * Creates an instance of the given skill type and loads it with the given config.
     * <p>The skill can then be added and applied to players.
     *
     * @param identifier the identifier of the skill
     * @param config the config to load the skill with
     * @return the loaded skill or an empty optional if the skill type was not found
     */
    public Optional<ConfiguredSkill> loadSkill(String identifier, ConfigurationSection config) {

        if (config == null) {
            return Optional.empty();
        }

        String skillType = config.getString("type", "permission");

        if (!hasType(skillType)) {
            log.severe("Unable to load skill " + identifier + ": skill type " + skillType + " does not exist!");
            return Optional.empty();
        }

        final UUID id = UUID.fromString(Objects.requireNonNull(config.getString("id", UUID.randomUUID().toString())));
        config.set("alias", config.getString("alias", identifier));
        String alias = config.getString("alias");

        ConfiguredSkill skill = Optional.ofNullable(ConfiguredSkill.find.byId(id))
                .or(() -> ConfiguredSkill.findByAliasOrName(alias))
                .orElseGet(() -> ConfiguredSkill.getOrCreate(id));


        ConfiguredSkill configuredSkill = skill.load(config);
        config.set("id", skill.id().toString());
        config.set("enabled", skill.enabled());

        if (!RCSkills.isTesting()) {
            PluginManager pluginManager = Bukkit.getPluginManager();
            configuredSkill.requirements().stream()
                    .filter(r -> r instanceof PermissionRequirement)
                    .flatMap(r -> ((PermissionRequirement) r).getPermissions().stream())
                    .map(s -> new Permission(s, PermissionDefault.FALSE))
                    .forEach(p -> {
                        pluginManager.removePermission(p);
                        pluginManager.addPermission(p);
                    });
        }

        ConfigurationSection skills = config.getConfigurationSection(SUB_SKILL_SECTION);
        if (skills != null) {
            for (String key : skills.getKeys(false)) {
                ConfigurationSection section = skills.getConfigurationSection(key);
                if (section == null) continue;
                section.set("parent", skill.id().toString());
                String subIdentifier = config.getString("alias") + ":" + key;
                loadSkill(
                        subIdentifier,
                        section
                ).ifPresent(subSkill -> log.info("loaded sub skill " + subIdentifier + " of " + identifier));
            }
        }

        return Optional.of(configuredSkill);
    }

    /**
     * Tries to get skill type registration of the given identifier.
     * <p>Use the {@link Skill.Registration} to create new instances of the
     * skill and then call {@link Skill#load(ConfigurationSection)} to load it from a config.
     *
     * @param type the type identifier
     *             must not be null
     * @return the skill type registration or an empty optional
     */
    public Optional<Skill.Registration<?>> getSkillType(String type) {

        if (Strings.isNullOrEmpty(type)) {
            return Optional.empty();
        }

        return Optional.ofNullable(skillTypes().get(type.toLowerCase()));
    }

    /**
     * Gets the tpye of a skill based on the provided class.
     *
     * @param skillClass the skill class to get the type from
     * @throws RuntimeException if the skill is not annotated with @SkillInfo
     * @return the type of the skill
     */
    public String getSkillType(Class<? extends Skill> skillClass) {

        if (!skillClass.isAnnotationPresent(SkillInfo.class)) {
            throw new RuntimeException("Cannot get type of skill " + skillClass.getCanonicalName() + "! It is missing the @SkillInfo annotation.");
        }

        return skillClass.getAnnotation(SkillInfo.class).value();
    }

    /**
     * Checks if the given skill type exists.
     *
     * @param type the skill type to check. can be null.
     * @return true if the skill type exists and is loaded
     */
    public boolean hasType(String type) {

        return !Strings.isNullOrEmpty(type) && skillTypes().containsKey(type.toLowerCase());
    }

    /**
     * Loads the given skill from the cache or creates a new unique instance for the given player skill.
     * <p>This will create an instance of the skill with the provided supplier and load any config
     * options defined inside the skill class.
     * <p>The result of the load operation will be cached and retrieved on subsequent calls.
     *
     * @param playerSkill the player skill that should be loaded
     * @return the loaded skill.
     *         null if the skill type does not exist
     *         or null if the load of the configuration failed.
     */
    public SkillContext loadSkill(@NonNull PlayerSkill playerSkill) {

        UUID playerId = playerSkill.player().id();
        Map<UUID, SkillContext> cachedSkills = cachedPlayerSkills.getOrDefault(playerId, new HashMap<>());
        if (cachedSkills.containsKey(playerSkill.id())) {
            return cachedSkills.get(playerSkill.id());
        }

        DefaultSkillContext context = getSkillType(playerSkill.configuredSkill().type())
                .map(registration -> {
                    try {
                        DefaultSkillContext skillContext = new DefaultSkillContext(playerSkill, registration).init();
                        if (plugin.getPluginConfig().isDebug()) {
                            log.info("loaded skill context of " + playerSkill.configuredSkill().alias() + " for " + playerSkill.player().name());
                            log.info(skillContext.get().toString());
                        }
                        return skillContext;
                    } catch (ConfigurationException e) {
                        log.severe("Failed to load skill " + registration.skillClass().getCanonicalName() + ": " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    }
                }).orElse(null);

        if (context != null) {
            cachedSkills.put(playerSkill.id(), context);
            cachedPlayerSkills.put(playerId, cachedSkills);
        }

        return context;
    }

    public boolean isExecutable(ConfiguredSkill skill) {

        return getSkillType(skill.type()).map(Skill.Registration::executableSkill).orElse(false);
    }
}
