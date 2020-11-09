package net.silthus.skills;

import io.ebean.Database;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import net.silthus.skills.entities.SkilledPlayer;
import net.silthus.skills.requirements.PermissionRequirement;
import net.silthus.skills.requirements.SkillRequirement;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Log
@Getter
@Accessors(fluent = true)
public final class SkillManager {

    @Getter
    private static SkillManager instance;

    private final Map<String, Requirement.Registration> requirements = new HashMap<>();
    private final Map<String, Skill.Registration> skills = new HashMap<>();

    private final Database database;

    SkillManager(Database database) {
        this.database = database;
        instance = this;
    }

    void registerDefaults() {

        registerRequirement(PermissionRequirement::new);
        registerRequirement(() -> new SkillRequirement(this));
    }

    public SkillManager registerRequirement(Supplier<Requirement> supplier) {

        Requirement requirement = supplier.get();
        if (requirements.containsKey(requirement.identifier())) {
            log.severe("Cannot register skill requirement: " + requirement.getClass()
                    + "! A requirement with the same identifier '"
                    + requirement.identifier() + "' is already registered: "
                    + requirements.get(requirement.identifier()).requirementClass().getCanonicalName());
            return this;
        }

        requirements.put(requirement.identifier(), new Requirement.Registration(requirement.identifier(), requirement.getClass(), supplier));
        return this;
    }

    public SkillManager unregisterRequirement(Class<? extends Requirement> requirement) {

        requirements.values().stream().filter(registration -> registration.requirementClass().equals(requirement))
                .forEach(registration -> requirements.remove(registration.identifier()));
        return this;
    }

    public SkillManager registerSkill(Supplier<Skill> supplier) {

        Skill skill = supplier.get();
        if (skills.containsKey(skill.identifier())) {
            log.severe("Cannot register skill: " + skill.getClass()
                    + "! A skill with the same identifier '"
                    + skill.identifier() + "' is already registered: "
                    + skills.get(skill.identifier()).skillClass().getCanonicalName());
            return this;
        }

        skills.put(skill.identifier(), new Skill.Registration(skill.identifier(), skill.getClass(), supplier));
        return this;
    }

    public SkillManager unregisterSkill(Class<? extends Skill> skill) {

        skills.values().stream().filter(registration -> registration.skillClass().equals(skill))
                .forEach(registration -> skills.remove(registration.identifier()));
        return this;
    }

    public SkilledPlayer getPlayer(OfflinePlayer player) {

        return Optional.ofNullable(database()
                .find(SkilledPlayer.class, player.getUniqueId()))
                .orElse(new SkilledPlayer(player));
    }

    public List<Requirement> loadRequirements(ConfigurationSection config) {

        if (config == null) return new ArrayList<>();

        ArrayList<Requirement> requirements = new ArrayList<>();

        for (String requirementKey : config.getKeys(false)) {
            if (requirements().containsKey(requirementKey)) {
                ConfigurationSection requirementSection = config.getConfigurationSection(requirementKey);
                requirements.add(requirements().get(requirementKey).supplier().get().load(requirementSection));
            } else {
                log.warning("unable to find a requirement for " + requirementKey + " in " + config.getRoot().getName());
            }
        }

        return requirements;
    }

    public Optional<Skill> getSkill(String identifier) {

        return Optional.ofNullable(skills.get(identifier))
                .map(Skill.Registration::supplier)
                .map(Supplier::get);
    }
}
