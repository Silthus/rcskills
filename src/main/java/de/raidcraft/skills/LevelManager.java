package de.raidcraft.skills;

import de.raidcraft.skills.entities.LevelHistory;
import de.raidcraft.skills.entities.SkilledPlayer;
import de.raidcraft.skills.events.SetPlayerExpEvent;
import de.raidcraft.skills.events.SetPlayerLevelEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
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
    private final SkillsPlugin plugin;
    private final Map<UUID, Map<Integer, Integer>> cache = new HashMap<>();
    private Map<Integer, Integer> levelToExpMap = new HashMap<>();
    private Map<UUID, Map.Entry<BossBar, BukkitTask>> activeExpBars = new HashMap<>();

    @Getter(AccessLevel.PACKAGE)
    @Accessors(fluent = true)
    private IExpressionEvaluator ee;
    private double x;
    private double y;
    private double z;

    public LevelManager(SkillsPlugin plugin) {
        this.plugin = plugin;
    }

    public SkillPluginConfig.LevelConfig getConfig() {

        return getPlugin().getPluginConfig().getLevelConfig();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onExpGain(SetPlayerExpEvent event) {

        int level = getLevelForExp(event.getNewExp());
        event.setLevel(level);

        LevelHistory.create(event.getPlayerLevel())
                .oldExp(event.getOldExp())
                .newExp(event.getNewExp())
                .newLevel(event.getLevel())
                .reason(event.getReason())
                .save();

        event.getPlayer().getBukkitPlayer().ifPresent(player -> {
            Audience audience = BukkitAudiences.create(plugin)
                    .player(player);

            Map.Entry<BossBar, BukkitTask> expBar = activeExpBars.remove(player.getUniqueId());
            if (expBar != null) {
                audience.hideBossBar(expBar.getKey());
                expBar.getValue().cancel();
            }

            BossBar bossBar = Messages.levelProgressBar(level, event.getNewExp() - calculateTotalExpForLevel(level), calculateExpForNextLevel(level + 1));
            audience.showBossBar(bossBar);
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                        Map.Entry<BossBar, BukkitTask> entry = activeExpBars.remove(player.getUniqueId());
                        if (entry != null) {
                            BukkitAudiences.create(plugin)
                                    .player(player.getUniqueId())
                                    .hideBossBar(entry.getKey());
                        }
            },
            plugin.getPluginConfig().getExpProgressBarDuration());

            activeExpBars.put(player.getUniqueId(), Map.entry(bossBar, bukkitTask));
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onLevelUp(SetPlayerLevelEvent event) {

        int minExp = calculateTotalExpForLevel(event.getNewLevel());
        int maxExp = calculateTotalExpForLevel(event.getNewLevel() + 1);
        if (event.getExp() < minExp || event.getExp() >= maxExp) {
            event.setExp(minExp);
        }

        event.getPlayer().getBukkitPlayer().ifPresent(player -> {
            if (event.getNewLevel() > event.getOldLevel()) {
                Messages.send(player, Messages.levelUpSelf(event.getPlayer(), event.getNewLevel()));
                BukkitAudiences.create(plugin)
                        .player(player)
                        .showTitle(Messages.levelUpTitle(event.getNewLevel()));
                Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !p.equals(player))
                        .forEach(p -> Messages.send(p, Messages.levelUp(event.getPlayer())));
            } else if (event.getNewLevel() < event.getOldLevel()) {
                Messages.send(player, Messages.levelDownSelf(event.getPlayer(), event.getNewLevel()));
                Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !p.equals(player))
                        .forEach(p -> Messages.send(p, Messages.levelDown(event.getPlayer())));
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

        ee.cook(getConfig().getExpToNextLevel());
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
        if (playerCache.containsKey(player.level().getLevel())) {
            return Optional.of(playerCache.get(player.level().getLevel()));
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
                cache(player.id(), player.level().getLevel(),
                        calculateExpForNextLevel(player.level().getLevel())));
    }

    public int calculateExpToNextLevel(SkilledPlayer player) {

        return calculateExpToNextLevel(player, false);
    }

    public int calculateExpForNextLevel(int level) {

        try {
            return (int) Math.round((double) ee.evaluate(
                    x,
                    y,
                    z,
                    level));
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

            if (exp <= totalExp && level > lastLevel) {
                lastLevel = level;
            }
        }

        return lastLevel;
    }

    private int calculateTotalExpForLevel(final int level) {

        int sum = 0;
        for (int i = 1; i < level + 1; i++) {
            sum += calculateExpForNextLevel(i);
        }
        return sum;
    }
}
