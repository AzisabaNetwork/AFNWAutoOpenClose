package net.azisaba.afnw.autoOpenClose;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class AFNWAutoOpenClose extends JavaPlugin {
    private static final long fiveMinutes = 60 * 5L;
    private final Timer timer = new Timer();
    private final Map<DayOfWeek, DayData> map = new HashMap<>();
    private long lastChecked = System.currentTimeMillis() / 1000L; // in seconds

    @Override
    public void onEnable() {
        reload();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                var current = System.currentTimeMillis() / 1000L;
                LocalDate date = LocalDate.now();
                for (LocalTime open : map.get(date.getDayOfWeek()).opens()) {
                    long epoch = open.atDate(date).toEpochSecond(ZoneOffset.ofHours(9));
                    if (epoch - current < 0 && epoch - lastChecked > 0) {
                        Bukkit.getScheduler().runTask(AFNWAutoOpenClose.this, () -> {
                            Objects.requireNonNull(Bukkit.getWorld("lobby")).getBlockAt(0, 65, 5).setType(Material.AIR);
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "notice AFNW開場しました。");
                        });
                    }
                }
                for (LocalTime close : map.get(date.getDayOfWeek()).closes()) {
                    long epoch = close.atDate(date).toEpochSecond(ZoneOffset.ofHours(9));
                    if (epoch - current < fiveMinutes && epoch - lastChecked > fiveMinutes) {
                        Bukkit.getScheduler().runTask(AFNWAutoOpenClose.this, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "notice AFNWはあと5分で閉場します。"));
                    }
                    if (epoch - current < 0 && epoch - lastChecked > 0) {
                        Bukkit.getScheduler().runTask(AFNWAutoOpenClose.this, () -> {
                            Objects.requireNonNull(Bukkit.getWorld("lobby")).getBlockAt(0, 65, 5).setType(Material.REDSTONE_BLOCK);
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "notice AFNW閉場です。10秒後にロビーに転送します。");
                            Location lobbySpawnLocation = Objects.requireNonNull(Bukkit.getWorld("lobby")).getSpawnLocation();
                            Bukkit.getScheduler().runTaskLater(AFNWAutoOpenClose.this, () -> {
                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    if (player.getWorld().getName().startsWith("world")) {
                                        player.teleport(lobbySpawnLocation);
                                    }
                                }
                            }, 20 * 10);
                        });
                    }
                }
                lastChecked = current;
            }
        }, 10000, 10000);
    }

    @Override
    public void onDisable() {
        timer.cancel();
    }

    private void reload() {
        map.clear();
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            var section = getConfig().getConfigurationSection(dayOfWeek.name());
            if (section == null) {
                getLogger().info("Missing " + dayOfWeek.name());
                map.put(dayOfWeek, DayData.EMPTY);
                continue;
            }
            List<LocalTime> opens = section.getStringList("opens").stream().map(LocalTime::parse).toList();
            List<LocalTime> closes = section.getStringList("closes").stream().map(LocalTime::parse).toList();
            DayData data = new DayData(opens, closes);
            map.put(dayOfWeek, data);
        }
    }
}
