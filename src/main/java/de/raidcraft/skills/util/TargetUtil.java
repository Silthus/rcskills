package de.raidcraft.skills.util;

import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TargetUtil {

    public static Player getTargetPlayer(final Player player) {

        return getTarget(player, player.getWorld().getPlayers());
    }

    public static LivingEntity getTargetEntity(final LivingEntity entity, final Class<? extends LivingEntity> type) {

        if (entity instanceof Creature) {
            return ((Creature) entity).getTarget();
        }
        return getTarget(entity, entity.getWorld().getEntitiesByClass(type));
    }

    public static <T extends LivingEntity> T getTarget(final LivingEntity source, Class<T> targetClass, int range) {

        return getTarget(source, getNearbyEntities(source, range).stream()
                .filter(targetClass::isInstance)
                .map(targetClass::cast)
                .collect(Collectors.toSet())
        );
    }

    public static <T extends LivingEntity> T getTarget(final LivingEntity source, final Iterable<T> entities) {

        T target = null;
        double targetDistanceSquared = 0;
        final double radiusSquared = 1;
        final Vector l = source.getEyeLocation().toVector(), n = source.getLocation().getDirection().normalize();
        final double cos45 = Math.cos(Math.PI / 4);
        for (final T other : entities) {
            if (other == source) {
                continue;
            }
            if (target == null || targetDistanceSquared > other.getLocation().distanceSquared(source.getLocation())) {
                final Vector t = other.getLocation().add(0, 1, 0).toVector().subtract(l);
                if (n.clone().crossProduct(t).lengthSquared() < radiusSquared && t.normalize().dot(n) >= cos45) {
                    target = other;
                    targetDistanceSquared = target.getLocation().distanceSquared(source.getLocation());
                }
            }
        }
        return target;
    }

    public static List<LivingEntity> getNearbyEntities(final Entity source, int radius) {

        return source.getNearbyEntities(radius, radius, radius).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .collect(Collectors.toList());
    }

    public static List<LivingEntity> getLivingEntitiesInCone(LivingEntity source, float radius) {

        return getLivingEntitiesInCone(source, radius, 45.0F);
    }

    public static List<LivingEntity> getLivingEntitiesInCone(LivingEntity source, float radius, float degrees) {

        return getEntitiesInCone(
                getNearbyEntities(source, (int) radius),
                source.getLocation().toVector(),
                radius,
                degrees,
                source.getEyeLocation().toVector());
    }


    /**
     * @param entities  List of nearby entities
     * @param startPos  starting position
     * @param radius    distance cone travels
     * @param degrees   angle of cone
     * @param direction direction of the cone
     *
     * @return All entities inside the cone
     */
    public static List<LivingEntity> getEntitiesInCone(List<LivingEntity> entities, Vector startPos, float radius, float degrees, Vector direction) {

        // Returned list
        List<LivingEntity> newEntities = new ArrayList<>();
        // We don't want to use square root
        float squaredRadius = radius * radius;

        for (Entity entity : entities) {
            Vector relativePosition = entity.getLocation().toVector();
            // Position of the entity relative to the cone origin
            relativePosition.subtract(startPos);
            // First check : distance
            if (relativePosition.lengthSquared() > squaredRadius) continue;
            // Second check : angle
            if (getAngleBetweenVectors(direction, relativePosition) > degrees) continue;
            if (entity instanceof LivingEntity) {
                newEntities.add((LivingEntity) entity);
            }
        }
        return newEntities;
    }

    /**
     * @param startPos  starting position
     * @param radius    distance cone travels
     * @param degrees   angle of cone
     * @param direction direction of the cone
     *
     * @return All block positions inside the cone
     */
    public static List<Vector> getPositionsInCone(Vector startPos, float radius, float degrees, Vector direction) {

        // Returned list
        List<Vector> positions = new ArrayList<>();
        // We don't want to use square root
        float squaredRadius = radius * radius;

        for (float x = startPos.getBlockX() - radius; x < startPos.getBlockX() + radius; x++) {
            for (float y = startPos.getBlockY() - radius; y < startPos.getBlockY() + radius; y++) {
                for (float z = startPos.getBlockZ() - radius; z < startPos.getBlockZ() + radius; z++) {
                    Vector relative = new Vector(x, y, z);
                    relative.subtract(startPos);
                    // First check : distance
                    if (relative.lengthSquared() > squaredRadius) continue;
                    // Second check : angle
                    if (getAngleBetweenVectors(direction, relative) > degrees) continue;
                    // The position v is in the cone
                    positions.add(new Vector(x, y, z));
                }
            }
        }
        return positions;
    }


    public static float getAngleBetweenVectors(Vector v1, Vector v2) {

        return Math.abs((float) Math.toDegrees(v1.angle(v2)));
    }

    public static float lookAtIgnoreY(double x, double z, double lookAtX, double lookAtZ) {

        // Values of change in distance (make it relative)
        double dx = x - lookAtX;
        double dz = z - lookAtZ;

        double myyaw = 0;
        // Set yaw
        if (dx != 0) {
            // Set yaw startStage value based on dx
            if (dx < 0) {
                myyaw = 1.5 * Math.PI;
            } else {
                myyaw = 0.5 * Math.PI;
            }
            myyaw = myyaw - Math.atan(dz / dx);
        } else if (dz < 0) {
            myyaw = Math.PI;
        }

        // Set values, convert to degrees (invert the yaw since Bukkit uses a different yaw dimension format)
        return (float) (-myyaw * 180f / Math.PI);
    }
}
