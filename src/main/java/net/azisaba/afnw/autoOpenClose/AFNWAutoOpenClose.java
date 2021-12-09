package net.azisaba.afnw.autoOpenClose;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class AFNWAutoOpenClose extends JavaPlugin {
    @Override
    public void onEnable() {
        Objects.requireNonNull(Bukkit.getPluginCommand("afnwclose")).setExecutor((sender, command, label, args) -> {
            Objects.requireNonNull(Bukkit.getWorld("lobby")).getBlockAt(0, 65, 5).setType(Material.REDSTONE_BLOCK);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "broadcast AFNW閉場です。10秒後にロビーに転送します。");
            Location lobbySpawnLocation = Objects.requireNonNull(Bukkit.getWorld("lobby")).getSpawnLocation();
            Bukkit.getScheduler().runTaskLater(AFNWAutoOpenClose.this, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld().getName().startsWith("world")) {
                        player.teleport(lobbySpawnLocation);
                    }
                }
            }, 20 * 10);
            return true;
        });
        Objects.requireNonNull(Bukkit.getPluginCommand("afnwopen")).setExecutor((sender, command, label, args) -> {
            Objects.requireNonNull(Bukkit.getWorld("lobby")).getBlockAt(0, 65, 5).setType(Material.AIR);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "broadcast AFNW開場しました。");
            return true;
        });
    }
}
