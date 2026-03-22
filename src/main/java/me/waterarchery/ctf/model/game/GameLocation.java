package me.waterarchery.ctf.model.game;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

public record GameLocation(String world, double x, double y, double z, float yaw, float pitch) {

    public static GameLocation fromLocation(@NotNull Location location) {
        return new GameLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(),
            location.getYaw(), location.getPitch());
    }

    public Location toLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    public double distance(GameLocation o) {
        return Math.sqrt(distanceSquared(o));
    }

    // from Spigot's code
    public double distanceSquared(GameLocation o) {
        if (o == null) {
            throw new IllegalArgumentException("Cannot measure distance to a null location");
        } else if (o.world() == null || world == null) {
            throw new IllegalArgumentException("Cannot measure distance to a null world");
        } else if (!o.world().equals(world)) {
            throw new IllegalArgumentException("Cannot measure distance between " + world + " and " + o.world());
        }

        return NumberConversions.square(x - o.x) + NumberConversions.square(y - o.y) + NumberConversions.square(z - o.z);
    }
}
