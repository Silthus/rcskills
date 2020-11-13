package net.silthus.skills;

import io.ebean.Database;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import net.silthus.skills.entities.PlayerSkill;
import net.silthus.skills.entities.SkilledPlayer;
import net.silthus.skills.requirements.PermissionRequirement;
import net.silthus.skills.requirements.SkillRequirement;
import net.silthus.skills.skills.PermissionSkill;
import net.silthus.skills.util.ConfigUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log(topic = "sSkills")
@Getter
@Accessors(fluent = true)
public final class SkillManager {

    private final Map<String, Requirement.Registration<?>> requirements = new HashMap<>();
    private final Map<String, Skill.Registration<?>> skillTypes = new HashMap<>();

    private final Map<String, Skill> loadedSkills = new HashMap<>();
    private final Map<UUID, List<Skill>> loadedPlayerSkills = new HashMap<>();

    private final SkillsPlugin plugin;
    private final Database database;
    private final SkillPluginConfig config;

    SkillManager(SkillsPlugin plugin, Database database, SkillPluginConfig config) {
        this.plugin = plugin;
        this.database = database;
        this.config = config;
    }

    /**
     * Registers default requirements and skill types provided by this plugin.
     */
    void registerDefaults() {

        registerRequirement(PermissionRequirement.class, PermissionRequirement::new);
        registerRequirement(SkillRequirement.class, () -> new SkillRequirement(this));
        registerSkill(PermissionSkill.class, () -> new PermissionSkill(plugin));
    }

    public void reload() {

        unload();
        load();
    }

    public void load() {

        loadSkills(new File(plugin.getDataFolder(), config.getSkillsPath()).toPath());
    }

    public void unload() {

        loadedSkills.clear();
    }

    /**
     * Recursively loads all skill configs in the given path, creates and caches new skill instances for them.
     * Each file will be loaded and passed into {@link #loadSkill(String, ConfigurationSection)}.
     *
     * @param path the path to the skill configs
     */
    public List<Skill> loadSkills(Path path) {

        try {
            Files.createDirectories(path);
            List<File> files = Files.find(path, Integer.MAX_VALUE,
                    (file, fileAttr) -> fileAttr.isRegularFile())
                    .map(Path::toFile).collect(Collectors.toList());


            int fileCount = files.size();
            List<Skill> skills = files.stream().map(file -> loadSkill(path, file))
                    .flatMap(skill -> skill.stream().flatMap(Stream::of))
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
    public Optional<Skill> loadSkill(Path base, @NonNull File file) {

        if (!file.exists() || !(file.getName().endsWith(".yml") && !file.getName().endsWith(".yaml"))) {
            return Optional.empty();
        }

        try {
            YamlConfiguration config = new YamlConfiguration();
            config.load(file);
            config.set("id", config.getString("id", ConfigUtil.getFileIdentifier(base, file)));
            return loadSkill(config.getString("type", "permission"), config);
        } catch (IOException | InvalidConfigurationException e) {
            log.severe("unable to load skill config " + file.getAbsolutePath() + ": " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Registers a new requirement type with this skill manager.
     * <p>Requirement types are used to instantiate and load {@link Requirement}s as they are needed.
     * <p>Make sure your requirement is tagged with a @{@link RequirementType} annotation and has a unique type identifier.
     *
     * @param requirementClass the class of the requirement type
     * @param supplier the supplier that can create the requirement
     * @param <TRequirement> the type of the requirement
     * @return this skill manager
     */
    public <TRequirement extends Requirement> SkillManager registerRequirement(Class<TRequirement> requirementClass, Supplier<TRequirement> supplier) {

        if (!requirementClass.isAnnotationPresent(RequirementType.class)) {
            log.severe("Cannot register requirement " + requirementClass.getCanonicalName() + " without a @RequirementType annotation.");
            return this;
        }

        String type = requirementClass.getAnnotation(RequirementType.class).value().toLowerCase();
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
     * <p>Make sure your skill class is annotated with @{@link SkillType} or the registration will fail.
     * <p>The provided supplier will be used to create instances of the given skill type which are then loaded
     * and applied to players.
     *
     * @param skillClass the class of the skill type to register
     * @param supplier the supplier that can create new instances of the skill
     * @param <TSkill> the type of the skill
     * @return this skill manager
     */
    public <TSkill extends Skill> SkillManager registerSkill(Class<TSkill> skillClass, Supplier<TSkill> supplier) {

        if (!skillClass.isAnnotationPresent(SkillType.class)) {
            log.severe("Cannot register skill " + skillClass.getCanonicalName() + " without a @SkillType annotation.");
            return this;
        }

        String type = skillClass.getAnnotation(SkillType.class).value().toLowerCase();
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

        if (!skill.isAnnotationPresent(SkillType.class)) {
            return this;
        }
        skillTypes.remove(skill.getAnnotation(SkillType.class).value().toLowerCase());
        return this;
    }

    /**
     * Gets an existing player from the database or creates a new record from the given player.
     * <p>This method takes an {@link OfflinePlayer} for easier access to skills while players are offline.
     * However the skill can only be applied to the player if he is online. Any interaction will fail silently while offline.
     *
     * @param player the player that should be retrieved or created
     * @return a skilled player from the database
     */
    public SkilledPlayer getPlayer(OfflinePlayer player) {

        return Optional.ofNullable(database()
                .find(SkilledPlayer.class, player.getUniqueId()))
                .orElse(new SkilledPlayer(player));
    }

    /**
     * Loads and applies all active skills to the given player.
     * <p>Will do nothing if the player is already loaded.
     *
     * @param player the player that should be loaded
     */
    public void load(@NonNull Player player) {

        if (loadedPlayerSkills.containsKey(player.getUniqueId())) {
            return;
        }

        List<Skill> skills = new ArrayList<>();
        getPlayer(player).skills().stream().filter(PlayerSkill::unlocked)
                .map(playerSkill -> getSkill(playerSkill.identifier()))
                .flatMap(skill -> skill.stream().flatMap(Stream::of))
                .forEach(skill -> {
                    skill.apply(player);
                    skills.add(skill);
                });
        this.loadedPlayerSkills.put(player.getUniqueId(), skills);
    }

    /**
     * Unloads the player and all of his skills.
     * <p>Will do nothing if the player was never loaded.
     *
     * @param player the player to unload
     */
    public void unload(@NonNull Player player) {

        List<Skill> skills = loadedPlayerSkills.remove(player.getUniqueId());
        if (skills != null) {
            skills.forEach(skill -> skill.remove(player));
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
            if (!section.isSet("type")) {
                log.severe("requirement section "  + config.getName() + "." + requirementKey + " is missing the requirement type.");
                continue;
            }
            String type = section.getString("type");
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
     * <P>Loading the skill will also cache it inside {@link #loadedSkills()} and
     * make it available from the {@link #getSkill(String)} method.
     *
     * @param type the skill type to load
     * @param config the config to load the skill with
     * @return the loaded skill or an empty optional if the skill type was not found
     */
    public Optional<Skill> loadSkill(String type, ConfigurationSection config) {

        Optional<Skill> loadedSkill = getSkillType(type)
                .map(Skill.Registration::supplier)
                .map(Supplier::get)
                .map(skill -> skill.load(config));

        loadedSkill.ifPresent(skill -> {
            skill.addRequirements(loadRequirements(config.getConfigurationSection("requirements")));
            this.loadedSkills.put(skill.identifier(), skill);
        });

        return loadedSkill;
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
    public Optional<Skill.Registration<?>> getSkillType(@NonNull String type) {

        return Optional.ofNullable(skillTypes().get(type.toLowerCase()));
    }

    /**
     * Tries to get a loaded skill with the given identifier from the cache.
     * <p>Will return an empty optional if the skill is unknown or not loaded.
     * <p>Use the {@link #getSkillType(String)} method to get the raw skill type class.
     *
     * @param identifier the identifier of the skill as defined in the config
     *                   must not be null
     * @return the skill or an empty optional
     */
    public Optional<Skill> getSkill(@NonNull String identifier) {

        return Optional.ofNullable(loadedSkills().get(identifier.toLowerCase()));
    }
}
