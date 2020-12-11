package net.silthus.skills;

import com.google.common.collect.HashBiMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import net.silthus.skills.entities.SkilledPlayer;
import net.silthus.skills.events.SetPlayerExpEvent;
import net.silthus.skills.events.SetPlayerLevelEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.commons.compiler.IExpressionEvaluator;
import org.codehaus.janino.CompilerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Log(topic = "RCSkills")
public final class LevelManager implements Listener {

    @Getter
    private final SkillPluginConfig.LevelConfig config;
    private final Map<UUID, Map<Integer, Integer>> cache = new HashMap<>();
    private Map<Integer, Integer> levelToExpMap = new HashMap<>();

    @Getter(AccessLevel.PACKAGE)
    @Accessors(fluent = true)
    private IExpressionEvaluator ee;
    private double x;
    private double y;
    private double z;

    public LevelManager(SkillPluginConfig.LevelConfig config) {
        this.config = config;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onExpGain(SetPlayerExpEvent event) {

        int level = getLevelForExp(event.getNewExp());
        event.getLevel().level(level);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onLevelUp(SetPlayerLevelEvent event) {

        event.getPlayer().getBukkitPlayer().ifPresent(player -> {
            if (event.getNewLevel() > event.getOldLevel()) {
                player.sendMessage(ChatColor.GREEN + "Du bist im Level aufgestiegen: Level " + event.getNewLevel() + " erreicht!");
            } else if (event.getNewLevel() < event.getOldLevel()) {
                player.sendMessage(ChatColor.RED + "Du bist im Level abgestiegen: Level " + event.getNewLevel());
            }
        });
    }

    public void load() throws CompileException {

        cache.clear();

        this.x = getConfig().getX();
        this.y = getConfig().getY();
        this.z = getConfig().getZ();

        ee = new CompilerFactory().newExpressionEvaluator();
        ee.setExpressionType(double.class);
        ee.setParameters(new String[] {
                "x",
                "y",
                "z",
                "level"
        }, new Class[] {
                double.class,
                double.class,
                double.class,
                int.class
        });

        ee.cook(config.getExpToNextLevel());
        this.levelToExpMap = calculateTotalExpMap(getConfig().getMaxLevel());
    }

    private Map<Integer, Integer> calculateTotalExpMap(int maxLevel) {

        if (ee == null) return new HashMap<>();

        HashMap<Integer, Integer> expMap = new HashMap<>();

        for (int i = 1; i < maxLevel + 1; i++) {
            expMap.put(i, calculateTotalExpForLevel(i));
        }
        return expMap;
    }

    public Optional<Integer> getCache(SkilledPlayer player) {

        if (!cache.containsKey(player.id())) {
            return Optional.empty();
        }

        Map<Integer, Integer> playerCache = cache.getOrDefault(player.id(), new HashMap<>());
        if (playerCache.containsKey(player.level().level())) {
            return Optional.of(playerCache.get(player.level().level()));
        }

        return Optional.empty();
    }

    public Map<Integer, Integer> clearCache(UUID player) {

        if (!cache.containsKey(player)) {
            return new HashMap<>();
        }
        return cache.remove(player);
    }

    private Integer cache(UUID playerId, int level, int result) {

        if (!cache.containsKey(playerId)) {
            cache.put(playerId, new HashMap<>());
        }
        Map<Integer, Integer> playerCache = cache.getOrDefault(playerId, new HashMap<>());
        playerCache.put(level, result);
        cache.put(playerId, playerCache);
        return result;
    }

    public int calculateExpToNextLevel(SkilledPlayer player, boolean clearCache) {

        if (clearCache) clearCache(player.id());

        return getCache(player).orElseGet(() ->
                cache(player.id(), player.level().level(),
                        calculateExpForLevel(player.level().level())));
    }

    public int calculateExpToNextLevel(SkilledPlayer player) {

        return calculateExpToNextLevel(player, false);
    }

    public int calculateExpForLevel(int level) {

        try {
            return (int) Math.ceil((double) ee.evaluate(
                    x,
                    y,
                    z,
                    level)
            );
        } catch (InvocationTargetException e) {
            log.severe("failed to calculate exp for level " + level + ": " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    public int getTotalExpForLevel(int level) {

        return levelToExpMap.getOrDefault(level, -1);
    }

    public int getLevelForExp(long totalExp) {

        int lastLevel = 1;
        for (Map.Entry<Integer, Integer> entry : levelToExpMap.entrySet()) {
            int level = entry.getKey();
            int exp = entry.getValue();

            if (exp < totalExp) {
                lastLevel = level;
            } else {
                return lastLevel;
            }
        }

        return lastLevel;
    }

    private int calculateTotalExpForLevel(int level) {

        int sum = 0;
        for (int i = 1; i < level + 1; i++) {
            sum += calculateExpForLevel(level);
        }
        return sum;
    }
}
