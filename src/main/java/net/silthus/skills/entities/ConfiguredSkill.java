package net.silthus.skills.entities;

import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.Index;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import net.silthus.skills.Requirement;
import net.silthus.skills.Skill;
import net.silthus.skills.SkillsPlugin;
import net.silthus.skills.TestResult;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import javax.persistence.*;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;

@Entity
@Getter
@Setter
@Table(name = "rcs_skills")
@Accessors(fluent = true)
public class ConfiguredSkill extends Model implements Skill {

    public static Optional<ConfiguredSkill> findByAliasOrName(String alias) {

        return find.query()
                .where().eq("alias", alias)
                .findOneOrEmpty();
    }

    public static final Finder<UUID, ConfiguredSkill> find = new Finder<>(ConfiguredSkill.class);

    @Id
    private String id;
    @Index
    private String alias;
    @Index
    private String name;
    private String type;
    private String description;
    private Map<String, Object> config = new HashMap<>();

    @Version
    Long version;

    @WhenCreated
    Instant whenCreated;

    @WhenModified
    Instant whenModified;

    @Transient
    private Skill skill;
    @Transient
    private final List<Requirement> requirements = new ArrayList<>();

    public ConfiguredSkill(Skill skill) {
        this.skill = skill;
    }

    public Optional<Skill> getSkill() {

        return Optional.ofNullable(skill);
    }

    @Override
    public void load(ConfigurationSection config) {
        this.id(config.getString("id"));
        this.alias = config.getString("alias");
        this.name = config.getString("name", alias());
        this.type = config.getString("type", "permission");
        this.description = config.getString("description");
        ConfigurationSection with = config.getConfigurationSection("with");
        if (with != null) {
            this.config = new HashMap<>();
            with.getKeys(true).forEach(key -> this.config.put(key, with.get(key)));
            this.skill.load(with);
        } else {
            this.skill.load(config.createSection("with"));
        }
        save();
    }

    @PostLoad
    private void postLoad() {

        this.skill = SkillsPlugin.instance().getSkillManager().getSkillType(type())
                .map(Registration::supplier)
                .map(Supplier::get)
                .orElse(null);
        getSkill().ifPresent(skill -> {
            if (config() != null) {
                MemoryConfiguration cfg = new MemoryConfiguration();
                for (Map.Entry<String, Object> entry : config().entrySet()) {
                    cfg.set(entry.getKey(), entry.getValue());
                }
                skill.load(cfg);
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
        this.requirements.addAll(Arrays.asList(requirements));
    }

    public TestResult test(SkilledPlayer player) {

        return requirements().stream()
                .map(requirement -> requirement.test(player))
                .reduce(TestResult::merge)
                .orElse(TestResult.ofSuccess());
    }
}
