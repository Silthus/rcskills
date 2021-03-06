package de.raidcraft.skills;

import com.google.common.base.Strings;
import de.raidcraft.skills.entities.*;
import de.raidcraft.skills.events.*;
import de.raidcraft.skills.util.Effects;
import io.ebean.annotation.Transactional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.commons.compiler.IExpressionEvaluator;
import org.codehaus.janino.CompilerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

@Log(topic = "RCSkills")
public final class LevelManager implements Listener {

    @Getter
    private final RCSkills plugin;
    private final Map<UUID, Map<Integer, Integer>> cache = new HashMap<>();
    private Map<Integer, Integer> levelToExpMap = new HashMap<>();
    private final Map<UUID, Map.Entry<BossBar, BukkitTask>> activeExpBars = new HashMap<>();

    @Getter(AccessLevel.PACKAGE)
    @Accessors(fluent = true)
    private IExpressionEvaluator ee;
    private double x;
    private double y;
    private double z;

    public LevelManager(RCSkills plugin) {
        this.plugin = plugin;
    }

    public SkillPluginConfig.LevelConfig getConfig() {

        return getPlugin().getPluginConfig().getLevelConfig();
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onExpGain(SetPlayerExpEvent event) {

        long expDiff = event.getNewExp() - event.getOldExp();
        if (expDiff == 0) return;

        int level = getLevelForExp(event.getNewExp());
        event.setLevel(level);

        LevelHistory.create(event.getPlayerLevel())
                .oldExp(event.getOldExp())
                .newExp(event.getNewExp())
                .newLevel(event.getLevel())
                .reason(event.getReason())
                .save();

        event.getPlayer().bukkitPlayer().ifPresent(player -> {
            Audience audience = BukkitAudiences.create(plugin)
                    .player(player);

            Map.Entry<BossBar, BukkitTask> expBar = activeExpBars.remove(player.getUniqueId());
            if (expBar != null) {
                audience.hideBossBar(expBar.getKey());
                expBar.getValue().cancel();
            }

            long exp = event.getNewExp() - calculateTotalExpForLevel(level);
            if (level == 1) {
                exp = event.getNewExp();
            }

            BossBar bossBar = Messages.levelProgressBar(level, exp, calculateExpForNextLevel(level));
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

            TextComponent.Builder builder = text();
            if (expDiff < 0) {
                builder.append(text("-", DARK_RED, BOLD)).append(text(expDiff, RED));
            } else {
                builder.append(text("+", DARK_GREEN, BOLD)).append(text(expDiff, GREEN));
            }
            builder.append(text(" EXP", YELLOW)).append(text(" - ", DARK_AQUA));
            if (!Strings.isNullOrEmpty(event.getReason())) {
                builder.append(text(event.getReason(), DARK_AQUA, ITALIC));
            }

            @NonNull BaseComponent[] baseComponents = BungeeComponentSerializer.get().serialize(builder.build());
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, baseComponents);

            activeExpBars.put(player.getUniqueId(), Map.entry(bossBar, bukkitTask));
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onLevelUp(SetPlayerLevelEvent event) {

        int minExp = calculateTotalExpForLevel(event.getNewLevel());
        int maxExp = calculateTotalExpForLevel(event.getNewLevel() + 1);
        if (event.getExp() < minExp || event.getExp() >= maxExp) {
            event.setExp(minExp);
        }
    }

    @EventHandler
    public void onSkillSlotChange(PlayerSkillSlotsChangedEvent event) {

        Messages.send(event.getPlayer().id(), Messages.addSkillSlots(event.getPlayer(), event.getNewSkillSlots() - event.getOldSkillSlots()));
    }

    @EventHandler
    public void onSkillUnlock(PlayerUnlockedSkillEvent event) {

        event.getPlayer().bukkitPlayer().ifPresent(player -> {
            Effects.playerUnlockSkill(player);

            TextComponent heading = text("Skill freigeschaltet", NamedTextColor.GOLD);
            TextComponent subheading = text("Skill '", NamedTextColor.GREEN).append(text(event.getSkill().name(), NamedTextColor.AQUA))
                    .append(text("' freigeschaltet!", NamedTextColor.GREEN));
            Title title = Title.title(heading, subheading);
            BukkitAudiences.create(RCSkills.instance())
                    .player(player)
                    .showTitle(title);
        });
    }

    @EventHandler
    @Transactional
    public void onLeveledUp(PlayerLeveledEvent event) {

        SkillPluginConfig.LevelUpConfig config = getPlugin().getPluginConfig().getLevelUpConfig();

        int levelDiff = event.getNewLevel() - event.getOldLevel();
        if (levelDiff == 0) return;
        boolean levelDown = levelDiff < 0;

        int skillpoints = levelDiff * config.getSkillPointsPerLevel();
        Map<SkillSlot.Status, Integer> skillSlots = new HashMap<>();
        skillSlots.put(SkillSlot.Status.valueOf(config.getSlotStatus()), levelDiff * config.getSlotsPerLevel());

        SkilledPlayer skilledPlayer = event.getPlayer();
        for (int i = 0; i < Math.abs(levelDiff); i++) {
            int level = event.getOldLevel() + 1 + (levelDown ? -i : i);
            SkillPluginConfig.LevelUp levelUp = config.getLevels().get(level);
            if (levelUp != null) {
                skillpoints += levelUp.getSkillpoints();
                if (levelUp.getSlots() > 0) {
                    SkillSlot.Status status = SkillSlot.Status.valueOf(levelUp.getSlotStats());
                    int slots = skillSlots.getOrDefault(status, 0);
                    slots += levelUp.getSlots();
                    skillSlots.put(status, slots);
                }
                if (!levelDown) {
                    levelUp.getCommands().stream()
                            .map(s -> s.replace("{player}", skilledPlayer.name()))
                            .map(s -> s.replace("{player_id}", skilledPlayer.id().toString()))
                            .forEach(s -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), s));
                }
            }
        }

        if (levelDown) {
            skilledPlayer.removeSkillPoints(skillpoints);
            skilledPlayer.resetSkillSlots();
            int sum = skillSlots.entrySet().stream()
                    .filter(statusIntegerEntry -> statusIntegerEntry.getKey() == SkillSlot.Status.FREE)
                    .mapToInt(Map.Entry::getValue).sum();
            skilledPlayer.setSkillSlots(skilledPlayer.slotCount() - sum, SkillSlot.Status.FREE);

            int removedFreeResets = sum * plugin.getPluginConfig().getSlotConfig().getFreeResetsPerSlot();
            int freeResets = skilledPlayer.freeResets() - removedFreeResets;
            if (freeResets < 0) freeResets = 0;
            freeResets++;

            skilledPlayer.freeResets(freeResets).save();
        } else {
            skilledPlayer.addSkillPoints(skillpoints);
            skillSlots.forEach((status, integer) -> skilledPlayer.addSkillSlots(integer, status));
        }

        int freeResetsPerSlot = plugin.getPluginConfig().getSlotConfig().getFreeResetsPerSlot();
        int freeResets = 0;

        int newSkillSlots = skillSlots.values().stream().mapToInt(value -> value).sum();

        if (!levelDown && freeResetsPerSlot > 0 && newSkillSlots > 0) {
            freeResets = freeResetsPerSlot * newSkillSlots;
            skilledPlayer.freeResets(freeResets + skilledPlayer.freeResets()).save();
        }

        List<PlayerSkill> skills = ConfiguredSkill.find.query()
                .where().eq("enabled", true)
                .and().le("level", event.getNewLevel())
                .and().gt("level", event.getOldLevel())
                .orderBy().desc("level")
                .findList().stream()
                .map(skill -> PlayerSkill.getOrCreate(skilledPlayer, skill))
                .collect(Collectors.toList());

        skills.stream()
                .filter(skill -> !skill.unlocked())
                .filter(skill -> !skill.isChild())
                .filter(skill -> skill.configuredSkill().autoUnlock())
                .forEach(skill -> skilledPlayer.addSkill(skill.configuredSkill()));

        skilledPlayer.skills().stream()
                .map(PlayerSkill::children)
                .flatMap(Collection::stream)
                .filter(skill -> !skill.unlocked())
                .filter(skill -> skill.configuredSkill().canAutoUnlock(skilledPlayer))
                .forEach(PlayerSkill::unlock);

        int finalSkillpoints = skillpoints;
        int finalFreeResets = freeResets;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            skilledPlayer.bukkitPlayer().ifPresent(player -> {
                if (event.getNewLevel() > event.getOldLevel()) {
                    Messages.send(player, Messages.levelUpSelf(skilledPlayer, event.getNewLevel()));
                    Messages.send(skilledPlayer.id(), Messages.addSkillPointsSelf(skilledPlayer, finalSkillpoints));
                    Messages.send(skilledPlayer.id(), Messages.addSkillSlotsSelf(skilledPlayer, newSkillSlots));
                    Messages.send(skilledPlayer.id(), Messages.addFreeResetsSelf(skilledPlayer, finalFreeResets));

                    Effects.levelUp(player);

                    if (skills.size() > 0) {
                        Messages.send(skilledPlayer.id(), text(skills.size(), GREEN)
                                .append(text(" neue(r) Skill(s) freigeschaltet: ", YELLOW)).append(newline())
                                .append(Messages.skills(skills))
                        );
                    }

                    BukkitAudiences.create(plugin)
                            .player(player)
                            .showTitle(Messages.levelUpTitle(event.getNewLevel()));
                    Bukkit.getOnlinePlayers().stream()
                            .filter(p -> !p.equals(player))
                            .forEach(p -> Messages.send(p, Messages.levelUp(skilledPlayer)));
                } else if (event.getNewLevel() < event.getOldLevel()) {
                    Messages.send(player, Messages.levelDownSelf(skilledPlayer, event.getNewLevel()));
                    Bukkit.getOnlinePlayers().stream()
                            .filter(p -> !p.equals(player))
                            .forEach(p -> Messages.send(p, Messages.levelDown(skilledPlayer)));
                }
            });
        }, 10L);
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
        for (int i = 1; i < level; i++) {
            sum += calculateExpForNextLevel(i);
        }
        return sum;
    }
}
