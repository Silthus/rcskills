package net.silthus.skills.entities;

import io.ebean.Finder;
import io.ebean.annotation.Index;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import net.silthus.skills.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import javax.persistence.*;
import java.util.*;
import java.util.function.Supplier;

@Entity
@Getter
@Setter
@Table(name = "rcs_skills")
@Accessors(fluent = true)
public class ConfiguredSkill extends BaseEntity implements Skill {

    public static Optional<ConfiguredSkill> findByAliasOrName(String alias) {

        return find.query()
                .where().eq("alias", alias)
                .findOneOrEmpty();
    }

    public static final Finder<UUID, ConfiguredSkill> find = new Finder<>(ConfiguredSkill.class);

    @Index
    private String alias;
    @Index
    private String name;
    private String type;
    private String description;
    private Map<String, Object> config = new HashMap<>();
    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "skill")
    private List<PlayerSkill> playerSkills = new ArrayList<>();

    @Transient
    private Skill skill;
    @Transient
    private List<Requirement> requirements;

    public ConfiguredSkill(Skill skill) {
        this.skill = skill;
    }

    public List<Requirement> requirements() {

        if (requirements == null) {
            this.requirements = new ArrayList<>();
        }
        return requirements;
    }

    public Optional<Skill> getSkill() {

        return Optional.ofNullable(skill);
    }

    @Override
    public void load(ConfigurationSection config) {
        this.alias = config.getString("alias");
        this.name = config.getString("name", alias());
        this.type = config.getString("type", "permission");
        this.description = config.getString("description");

        ConfigurationSection with = config.getConfigurationSection("with");
        this.skill.load(Objects.requireNonNullElseGet(with, () -> config.createSection("with")));

        this.config = new HashMap<>();
        config.getKeys(true).forEach(key -> this.config.put(key, config.get(key)));

        save();
    }

    @PostLoad
    private void postLoad() {

        SkillManager skillManager = SkillsPlugin.instance().getSkillManager();
        this.skill = skillManager.getSkillType(type())
                .map(Registration::supplier)
                .map(Supplier::get)
                .orElse(null);
        getSkill().ifPresent(skill -> {
            if (config() != null) {
                MemoryConfiguration cfg = new MemoryConfiguration();
                for (Map.Entry<String, Object> entry : config().entrySet()) {
                    cfg.set(entry.getKey(), entry.getValue());
                }
                ConfigurationSection with = cfg.getConfigurationSection("with");
                skill.load(Objects.requireNonNullElseGet(with, () -> cfg.createSection("with")));
                skillManager.loadRequirements(cfg.getConfigurationSection("requirements"));
            }
        });
    }

    @Override
    public void apply(SkilledPlayer player) {
        getSkill().ifPresent(skill -> skill.apply(player));
    }

    @Override
    public void remove(SkilledPlayer player) {
        getSkill().ifPresent(skill -> skill.remove(player));
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
