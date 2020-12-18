package de.raidcraft.skills;

import com.google.common.base.Strings;
import de.raidcraft.skills.entities.ConfiguredSkill;
import de.raidcraft.skills.entities.PlayerSkill;
import de.raidcraft.skills.entities.SkilledPlayer;
import de.raidcraft.skills.requirements.LevelRequirement;
import de.raidcraft.skills.requirements.MoneyRequirement;
import de.raidcraft.skills.requirements.PermissionRequirement;
import de.raidcraft.skills.requirements.SkillPointRequirement;
import de.raidcraft.skills.requirements.SkillRequirement;
import de.raidcraft.skills.skills.PermissionSkill;
import de.raidcraft.skills.util.ConfigUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import net.silthus.configmapper.ConfigurationException;
import net.silthus.configmapper.bukkit.BukkitConfigMap;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Log(topic = "RCSkills")
@Getter
@Accessors(fluent = true)
public final class SkillManager {

    private final Map<String, Requirement.Registration<?>> requirements = new HashMap<>();
    private final Map<String, Skill.Registration<?>> skillTypes = new HashMap<>();

    private final Map<UUID, Map<UUID, Skill>> cachedPlayerSkills = new HashMap<>();

    private final SkillsPlugin plugin;
    private final SkillPluginConfig config;

    public SkillManager(SkillsPlugin plugin, SkillPluginConfig config) {
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

        registerSkill(PermissionSkill.class, (skill) -> new PermissionSkill(skill, plugin));
    }

    public void reload() {

        unload();
        load();
    }

    public void load() {

        loadSkills(new File(plugin.getDataFolder(), config.getSkillsPath()).toPath());
    }

    public void unload() {

    }

    /**
     * Recursively loads all skill configs in the given path, creates and caches new skill instances for them.
     * Each file will be loaded and passed into {@link #loadSkill(String, ConfigurationSection)}.
     *
     * @param path the path to the skill configs
     */
    public List<ConfiguredSkill> loadSkills(Path path) {

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

            PluginManager pluginManager = Bukkit.getServer().getPluginManager();
            skills.stream()
                    .map(ConfiguredSkill::requirements)
                    .filter(r -> r instanceof PermissionRequirement)
                    .flatMap(r -> ((PermissionRequirement) r).getPermissions().stream())
                    .map(s -> new Permission(s, PermissionDefault.FALSE))
                    .forEach(pluginManager::addPermission);

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
    public Optional<ConfiguredSkill> loadSkill(Path base, @NonNull File file) {

        if (!file.exists() || !(file.getName().endsWith(".yml") && !file.getName().endsWith(".yaml"))) {
            return Optional.empty();
        }

        try {
            YamlConfiguration config = new YamlConfiguration();
            config.load(file);
            Optional<ConfiguredSkill> skill = loadSkill(ConfigUtil.getFileIdentifier(base, file), config);
            config.save(file);
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
     * Registers the given skill type with this skill manager.
     * <p>Make sure your skill class is annotated with @{@link SkillInfo} or the registration will fail.
     * <p>The provided supplier will be used to create instances of the given skill type which are then loaded
     * and applied to players.
     *
     * @param skillClass the class of the skill type to register
     * @param supplier the supplier that can create new instances of the skill
     * @param <TSkill> the type of the skill
     * @return this skill manager
     */
    public <TSkill extends Skill> SkillManager registerSkill(Class<TSkill> skillClass, Function<PlayerSkill, TSkill> supplier) {

        if (!skillClass.isAnnotationPresent(SkillInfo.class)) {
            log.severe("Cannot register skill " + skillClass.getCanonicalName() + " without a @SkillType annotation.");
            return this;
        }

        String type = skillClass.getAnnotation(SkillInfo.class).value().toLowerCase();
        if (skillTypes.containsKey(type)) {
            log.severe("Cannot register skill: " + skillClass.getCanonicalName()
                    + "! A skill with the same type identifier '"
                    + type + "' is already registered: "
                    + skillTypes.get(type).skillClass().getCanonicalName());
            return this;
        }

        skillTypes.put(type, new Skill.Registration<>(type, skillClass, supplier));
        log.info("registered skill type: " + type + " [" + skillClass.getCanonicalName() + "]");
        return this;
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

        SkilledPlayer.getOrCreate(player).activeSkills()
                .forEach(PlayerSkill::enable);
    }

    /**
     * Unloads the player and all of his skills.
     * <p>Will do nothing if the player was never loaded.
     *
     * @param player the player to unload
     */
    public void unload(@NonNull Player player) {

        SkilledPlayer.getOrCreate(player).activeSkills().forEach(PlayerSkill::deactivate);
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
                requirements.add(requirements().get(type).supplier().get().load(section));
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

        return Optional.of(skill.load(config));
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
    public Skill loadSkill(@NonNull PlayerSkill playerSkill) {

        UUID playerId = playerSkill.player().id();
        Map<UUID, Skill> cachedSkills = cachedPlayerSkills.getOrDefault(playerId, new HashMap<>());
        if (cachedSkills.containsKey(playerSkill.id())) {
            return cachedSkills.get(playerSkill.id());
        }

        Skill loadedSkill = getSkillType(playerSkill.configuredSkill().type())
                .map(registration -> {
                    try {
                        Skill skill = registration.supplier().apply(playerSkill);
                        ConfigurationSection skillConfig = playerSkill.configuredSkill().getSkillConfig();
                        skill = BukkitConfigMap.of(skill)
                                .with(skillConfig)
                                .applyTo(skill);
                        skill.load(skillConfig);
                        return skill;
                    } catch (ConfigurationException e) {
                        log.severe("Failed to load skill " + registration.skillClass().getCanonicalName() + ": " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    }
                }).orElse(null);

        if (loadedSkill != null) {
            cachedSkills.put(playerSkill.id(), loadedSkill);
            cachedPlayerSkills.put(playerId, cachedSkills);
        }

        return loadedSkill;
    }
}
