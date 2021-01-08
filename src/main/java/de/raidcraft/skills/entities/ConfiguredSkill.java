package de.raidcraft.skills.entities;

import com.google.common.base.Strings;
import de.raidcraft.skills.*;
import de.raidcraft.skills.requirements.*;
import io.ebean.Finder;
import io.ebean.annotation.DbDefault;
import io.ebean.annotation.DbJson;
import io.ebean.annotation.Index;
import io.ebean.text.json.EJson;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import net.silthus.ebean.BaseEntity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import javax.persistence.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Getter
@Setter
@Table(name = "rcs_skills")
@Accessors(fluent = true)
@Log(topic = "RCSkills")
public class ConfiguredSkill extends BaseEntity implements Comparable<ConfiguredSkill> {

    static {
        try {
            EJson.write(new Object());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public static Optional<ConfiguredSkill> findByAliasOrName(String alias) {

        return find.query()
                .where().eq("alias", alias)
                .findOneOrEmpty();
    }

    public static ConfiguredSkill getOrCreate(UUID id) {

        return Optional.ofNullable(find.byId(id)).orElse(new ConfiguredSkill(id));
    }

    public static List<ConfiguredSkill> allEnabled() {

        return find.query()
                .where().eq("enabled", true)
                .findList();
    }

    public static List<ConfiguredSkill> autoUnlockableSkills(int level) {

        return find.query()
                .where().eq("enabled", true)
                .and().isNull("parent")
                .and().eq("money", 0)
                .and().eq("skillpoints", 0)
                .and().eq("no_skill_slot", true)
                .and().eq("restricted", false)
                .and().eq("auto_unlock", true)
                .and().le("level", level)
                .findList();
    }

    public static final Finder<UUID, ConfiguredSkill> find = new Finder<>(ConfiguredSkill.class);

    @Index
    private String alias;
    @Index
    private String name = alias();
    private String type = "permission";
    private String description = "N/A";
    private int level = 1;
    private double money = 0d;
    private int skillpoints = 0;
    private boolean noSkillSlot = false;
    private boolean hidden = false;
    private boolean enabled = true;
    private boolean restricted = false;
    private boolean autoUnlock = false;
    private boolean autoActivate = true;

    @ManyToOne
    private ConfiguredSkill parent;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConfiguredSkill> children = new ArrayList<>();

    @DbJson
    private Map<String, Object> config = new HashMap<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayerSkill> playerSkills = new ArrayList<>();

    @DbJson
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    @DbDefault("[]")
    private List<String> replacedSkillIds = new ArrayList<>();

    @DbJson
    @Setter(AccessLevel.PACKAGE)
    @DbDefault("[]")
    private List<String> worlds = new ArrayList<>();

    @Transient
    private transient List<String> categories = new ArrayList<>();
    @Transient
    private transient List<Requirement> requirements = new ArrayList<>();
    @Transient
    private transient List<Requirement> costRequirements = new ArrayList<>();
    @Transient
    private transient boolean loaded = false;
    @Transient
    private transient ExecutionConfig executionConfig;
    @Transient
    private transient TaskConfig taskConfig;

    ConfiguredSkill(UUID id) {
        this.id(id);
    }

    public boolean disabled() {

        return !enabled();
    }

    public boolean hidden() {

        return disabled() || hidden;
    }

    public boolean visible() {

        return !hidden();
    }

    public boolean isChild() {

        return parent() != null;
    }

    public boolean isParent() {

        return !children().isEmpty();
    }

    public List<String> categories() {

        if (categories == null) {
            this.categories = new ArrayList<>();
            load(false);
        }
        return categories;
    }

    public List<Requirement> requirements() {

        if (requirements == null) {
            this.requirements = new ArrayList<>();
            load(false);
        }
        return requirements;
    }

    public List<Requirement> costRequirements() {

        if (costRequirements == null) {
            this.costRequirements = new ArrayList<>();
            load(false);
        }
        return costRequirements;
    }

    public ExecutionConfig executionConfig() {

        if (executionConfig == null) {
            this.executionConfig = new ExecutionConfig(new MemoryConfiguration());
            load(false);
        }
        return executionConfig;
    }

    public TaskConfig taskConfig() {

        if (taskConfig == null) {
            this.taskConfig = new TaskConfig(new MemoryConfiguration());
            load(false);
        }
        return taskConfig;
    }

    /**
     * Gets a list of skills that this skill disables when it is activated.
     *
     * @return list of skills that should be disabled
     */
    public List<ConfiguredSkill> replacedSkills() {

        return replacedSkillIds().stream()
                .map(UUID::fromString)
                .map(ConfiguredSkill.find::byId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public ConfiguredSkill load(ConfigurationSection config) {

        updateConfig(config);
        load(true);
        save();

        return this;
    }

    private void updateConfig(ConfigurationSection config) {

        this.config = new HashMap<>();
        config.getKeys(true)
                .stream()
                .filter(key -> !config.isConfigurationSection(key))
                .filter(key -> !key.startsWith(SkillManager.SUB_SKILL_SECTION))
                .forEach(key -> this.config.put(key, config.get(key)));
    }

    public ConfigurationSection getConfig() {

        MemoryConfiguration config = new MemoryConfiguration();
        for (Map.Entry<String, Object> entry : config().entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }

        return config;
    }

    public ConfigurationSection getSkillConfig() {

        ConfigurationSection config = getConfig();
        return Objects.requireNonNullElse(config.getConfigurationSection("with"), config.createSection("with"));
    }

    @PostLoad
    void postLoad() {

        load(true);
    }

    private void load(boolean force) {

        if (!force && loaded) return;

        ConfigurationSection config = getConfig();

        String parent = config.getString("parent");
        if (!Strings.isNullOrEmpty(parent)) {
            try {
                parent(ConfiguredSkill.find.byId(UUID.fromString(parent)));
                if (config.getBoolean("disable-parent", false)
                        || config.getBoolean("replace-parent", false)) {
                    replacedSkillIds().add(parent);
                }

                ConfigurationSection parentSkillConfig = parent().getSkillConfig();
                for (String key : parentSkillConfig.getKeys(true)) {
                    if (!config.isSet("with." + key)) {
                        config.set("with." + key, parentSkillConfig.get(key));
                    }
                }

                updateConfig(config);
            } catch (IllegalArgumentException e) {
                log.severe("the parent of " + id() + " is not a valid UUID.");
                e.printStackTrace();
            }
        }

        setAlias(config);
        setName(config);
        setType(config);
        setDescription(config);
        setLevel(config);
        setSkillpoints(config);
        setMoney(config);
        setNoSkillSlot(config);
        setEnabled(config);
        setRestricted(config);
        setAutoUnlock(config);
        setAutoActivate(config);
        setHidden(config);

        setCategories(config);
        setExecutionConfig(config);
        setTaskConfig(config);
        setRequirements(config);
        setDisabledSkills(config);
        setWorlds(config);

        if (restricted) {
            requirements.add(new PermissionRequirement().add(SkillsPlugin.SKILL_PERMISSION_PREFIX + alias).load(new MemoryConfiguration()));
        }

        if (level > 0) {
            LevelRequirement levelRequirement = new LevelRequirement();
            levelRequirement.load(new MemoryConfiguration());
            levelRequirement.setLevel(level);
            requirements.add(levelRequirement);
        }

        if (money > 0) {
            MoneyRequirement moneyRequirement = new MoneyRequirement();
            moneyRequirement.load(new MemoryConfiguration());
            moneyRequirement.setAmount(money);
            costRequirements.add(moneyRequirement);
        }

        if (skillpoints > 0) {
            SkillPointRequirement skillPointRequirement = new SkillPointRequirement();
            skillPointRequirement.load(new MemoryConfiguration());
            skillPointRequirement.setSkillpoints(this.skillpoints);
            costRequirements.add(skillPointRequirement);
        }

        if (isChild()) {
            SkillRequirement skillRequirement = new SkillRequirement();
            skillRequirement.load(new MemoryConfiguration());
            skillRequirement.skill(parent());
            skillRequirement.hidden(true);
            requirements.add(skillRequirement);
        }

        loaded(true);
    }

    public ConfiguredSkill enabled(boolean enabled) {

        if (enabled == this.enabled()) return this;
        this.enabled = enabled;
        if (enabled()) {
            playerSkills().forEach(PlayerSkill::enable);
        } else {
            playerSkills().forEach(PlayerSkill::disable);
        }
        return this;
    }

    public void addRequirement(Requirement... requirements) {
        this.requirements().addAll(Arrays.asList(requirements));
    }

    public boolean canAutoUnlock(SkilledPlayer player) {

        return canAutoUnlock()
                && test(player).success();
    }

    public boolean canAutoUnlock() {

        return autoUnlock()
                && noSkillSlot()
                && skillpoints() <= 0
                && money() <= 0;
    }

    public TestResult test(SkilledPlayer player) {

        if (disabled()) return TestResult.ofError("Der Skill " + alias() + " ist nicht aktiviert.");

        return Stream.concat(requirements().stream(), costRequirements().stream())
                .map(requirement -> requirement.test(player))
                .reduce(TestResult::merge)
                .orElse(TestResult.ofSuccess());
    }

    public TestResult testRequirements(SkilledPlayer player) {

        if (disabled()) return TestResult.ofError("Der Skill " + alias() + " ist nicht aktiviert.");

        return requirements().stream()
                .map(requirement -> requirement.test(player))
                .reduce(TestResult::merge)
                .orElse(TestResult.ofSuccess());
    }

    public TestResult testCosts(SkilledPlayer player) {

        if (disabled()) return TestResult.ofError("Der Skill " + alias() + " ist nicht aktiviert.");

        return costRequirements().stream()
                .map(requirement -> requirement.test(player))
                .reduce(TestResult::merge)
                .orElse(TestResult.ofSuccess());
    }

    private void setAlias(ConfigurationSection config) {

        alias(config.getString("alias", isChild() ? parent().alias() : alias()));
    }

    private void setName(ConfigurationSection config) {

        name(config.getString("name", isChild() ? parent().name() : name()));
    }

    private void setType(ConfigurationSection config) {

        type(config.getString("type", isChild() ? parent().type() : type()));
    }

    private void setDescription(ConfigurationSection config) {

        description(config.getString("description", isChild() ? parent().description() : description()));
    }

    private void setLevel(ConfigurationSection config) {

        level(config.getInt("level", isChild() ? parent().level() : level()));
    }

    private void setSkillpoints(ConfigurationSection config) {

        skillpoints(config.getInt("skillpoints", isChild() ? 0 : skillpoints()));
    }

    private void setMoney(ConfigurationSection config) {

        money(config.getDouble("money", isChild() ? 0 : money()));
    }

    private void setNoSkillSlot(ConfigurationSection config) {

        noSkillSlot(config.getBoolean("no-skill-slot", isChild() || noSkillSlot()));
    }

    private void setHidden(ConfigurationSection config) {

        boolean hidden = hidden() || (isChild() && canAutoUnlock());
        hidden(config.getBoolean("hidden", hidden));
    }

    private void setEnabled(ConfigurationSection config) {

        enabled(config.getBoolean("enabled", enabled));
    }

    private void setRestricted(ConfigurationSection config) {

        restricted(config.getBoolean("restricted", !isChild() && restricted()));
    }

    private void setAutoUnlock(ConfigurationSection config) {

        autoUnlock(config.getBoolean("auto-unlock", isChild() || autoUnlock()));
    }

    private void setAutoActivate(ConfigurationSection config) {

        autoActivate(config.getBoolean("auto-activate", isChild() ? parent().autoActivate() : autoActivate()));
    }

    private void setCategories(ConfigurationSection config) {

        if (config.isSet("categories")) {
            categories(config.getStringList("categories"));
        } else if (isChild()) {
            categories(parent().categories());
        }
    }

    private void setExecutionConfig(ConfigurationSection config) {

        if (config.isSet("execution")) {
            ConfigurationSection section = config.getConfigurationSection("execution");
            executionConfig(new ExecutionConfig(Objects.requireNonNullElseGet(section,
                    () -> config.createSection("execution"))));
        } else if (isChild()) {
            executionConfig(parent().executionConfig());
        } else {
            executionConfig(new ExecutionConfig(config.createSection("execution")));
        }
    }

    private void setTaskConfig(ConfigurationSection config) {

        if (config.isSet("task")) {
            ConfigurationSection section = config.getConfigurationSection("task");
            taskConfig(new TaskConfig(Objects.requireNonNullElseGet(section,
                    () -> config.createSection("task"))));
        } else if (isChild()) {
            taskConfig(parent().taskConfig());
        } else {
            taskConfig(new TaskConfig(config.createSection("task")));
        }
    }

    private void setRequirements(ConfigurationSection config) {

        this.requirements(SkillsPlugin.instance().getSkillManager()
                .loadRequirements(config.getConfigurationSection("requirements")));
        this.costRequirements = new ArrayList<>();
    }

    private void setDisabledSkills(ConfigurationSection config) {

        this.replacedSkillIds().addAll(config.getStringList("replaces"));
    }

    private void setWorlds(ConfigurationSection config) {

        this.worlds().addAll(config.getStringList("worlds"));
    }

    @Override
    public int compareTo(ConfiguredSkill o) {

        return Integer.compare(level(), o.level());
    }
}
