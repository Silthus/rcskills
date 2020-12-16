package net.silthus.skills.entities;

import io.ebean.Finder;
import io.ebean.annotation.DbJson;
import io.ebean.annotation.Index;
import io.ebean.text.json.EJson;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import net.silthus.skills.*;
import net.silthus.skills.requirements.LevelRequirement;
import net.silthus.skills.requirements.MoneyRequirement;
import net.silthus.skills.requirements.PermissionRequirement;
import net.silthus.skills.requirements.SkillPointRequirement;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import javax.persistence.*;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

@Entity
@Getter
@Setter
@Table(name = "rcs_skills")
@Accessors(fluent = true)
public class ConfiguredSkill extends BaseEntity implements Skill {

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

    public static ConfiguredSkill getOrCreate(UUID id, Skill skill) {

        return Optional.ofNullable(find.byId(id)).orElse(new ConfiguredSkill(id, skill));
    }

    public static final Finder<UUID, ConfiguredSkill> find = new Finder<>(ConfiguredSkill.class);

    @Index
    private String alias;
    @Index
    private String name;
    private String type;
    private String description;

    @DbJson
    private Map<String, Object> config = new HashMap<>();
    @OneToMany(cascade = CascadeType.REMOVE)
    private List<PlayerSkill> playerSkills = new ArrayList<>();

    @Transient
    private int level = 0;
    @Transient
    private double cost = 0d;
    @Transient
    private int skillpoints = 0;

    @Transient
    private Skill skill;
    @Transient
    private List<Requirement> requirements;

    ConfiguredSkill(UUID id, Skill skill) {
        this.id(id);
        this.skill = skill;
    }

    public List<Requirement> requirements() {

        if (requirements == null) {
            this.requirements = new ArrayList<>();
            load();
        }
        return requirements;
    }

    public Optional<Skill> skill() {

        if (skill == null) {
            load();
        }

        return Optional.ofNullable(skill);
    }

    @Override
    public void load(ConfigurationSection config) {
        this.config = new HashMap<>();
        config.getKeys(true).forEach(key -> this.config.put(key, config.get(key)));

        load();
        save();
    }

    @PostLoad
    public void load() {

        SkillManager skillManager = SkillsPlugin.instance().getSkillManager();
        if (skill == null) {
            skill = skillManager.getSkillType(type)
                    .map(Registration::supplier)
                    .map(Supplier::get)
                    .orElse(null);
        }

        if (skill != null && this.config != null) {
            MemoryConfiguration config = new MemoryConfiguration();
            for (Map.Entry<String, Object> entry : this.config.entrySet()) {
                config.set(entry.getKey(), entry.getValue());
            }

            this.alias = config.getString("alias");
            this.name = config.getString("name", alias());
            this.type = config.getString("type", "permission");
            this.description = config.getString("description");

            ConfigurationSection with = config.getConfigurationSection("with");
            skill.load(Objects.requireNonNullElseGet(with, () -> config.createSection("with")));
            this.requirements = skillManager.loadRequirements(config.getConfigurationSection("requirements"));

            if (config.isSet("level")) {
                this.level = config.getInt("level", 0);
                LevelRequirement levelRequirement = new LevelRequirement();
                levelRequirement.setLevel(level);
                requirements.add(levelRequirement);
            }

            if (config.isSet("money")) {
                this.cost = config.getDouble("money", 0d);
                MoneyRequirement moneyRequirement = new MoneyRequirement();
                moneyRequirement.setAmount(cost);
                requirements.add(moneyRequirement);
            }

            if (config.isSet("skillpoints")) {
                this.skillpoints = config.getInt("skillpoints", 0);
                SkillPointRequirement skillPointRequirement = new SkillPointRequirement();
                skillPointRequirement.setSkillpoints(this.skillpoints);
                requirements.add(skillPointRequirement);
            }

            requirements.add(new PermissionRequirement().add(SkillsPlugin.SKILL_PERMISSION_PREFIX + alias));
        }
    }

    @Override
    public void apply(SkilledPlayer player) {
        skill().ifPresent(skill -> skill.apply(player));
    }

    @Override
    public void remove(SkilledPlayer player) {
        skill().ifPresent(skill -> skill.remove(player));
    }

    public void addRequirement(Requirement... requirements) {
        this.requirements().addAll(Arrays.asList(requirements));
    }

    public TestResult test(SkilledPlayer player) {

        return requirements().stream()
                .map(requirement -> requirement.test(player))
                .reduce(TestResult::merge)
                .orElse(TestResult.ofSuccess());
    }
}
