package de.raidcraft.skills;

import de.raidcraft.skills.util.ReflectionUtil;
import de.raidcraft.skills.util.TargetUtil;
import lombok.extern.java.Log;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Log(topic = "RCSkills")
public final class TargetManager {

    private final SkillsPlugin plugin;
    private final Map<Class<?>, TargetResolver<?>> resolvers = new HashMap<>();

    public TargetManager(SkillsPlugin plugin) {
        this.plugin = plugin;
    }

    void load() {

        resolvers.clear();
        registerDefaults();
    }

    public <TTarget> Optional<TTarget> resolve(ExecutionContext context, Class<TTarget> targetClass) {

        return resolver(targetClass).flatMap(resolver -> resolver.resolve(context));
    }

    @SuppressWarnings("unchecked")
    public <TTarget> Optional<TargetResolver<TTarget>> resolver(Class<TTarget> targetClass) {

        return ReflectionUtil.getEntryForTarget(targetClass, resolvers)
                .map(targetResolver -> (TargetResolver<TTarget>) targetResolver);
    }

    public <TTarget> TargetManager register(Class<TTarget> targetClass, TargetResolver<TTarget> resolver) {

        if (resolvers.containsKey(targetClass)) {
            log.warning("a target resolver for " + targetClass.getCanonicalName() + " is already registered!");
            return this;
        }

        resolvers.put(targetClass, resolver);
        log.info("registered target resolver for: " + targetClass.getCanonicalName());

        return this;
    }

    private void registerDefaults() {

        register(Player.class, context -> Optional.ofNullable(TargetUtil.getTarget(context.player(), Player.class, context.config().range())));
    }
}

