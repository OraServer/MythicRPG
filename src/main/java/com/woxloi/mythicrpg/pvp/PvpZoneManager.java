package com.woxloi.mythicrpg.pvp;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PvP許可ゾーンの登録・判定。
 * ゾーンは矩形（2点のLocationで定義）で管理する。
 * 将来的にWorldGuard連携に切り替え可能な設計。
 */
public class PvpZoneManager {

    /** ゾーン定義 */
    public static class PvpZone {
        public final String id;
        public final String displayName;
        private final World world;
        private final int x1, y1, z1;
        private final int x2, y2, z2;

        public PvpZone(String id, String displayName, Location corner1, Location corner2) {
            this.id          = id;
            this.displayName = displayName;
            this.world       = corner1.getWorld();
            this.x1 = Math.min(corner1.getBlockX(), corner2.getBlockX());
            this.y1 = Math.min(corner1.getBlockY(), corner2.getBlockY());
            this.z1 = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
            this.x2 = Math.max(corner1.getBlockX(), corner2.getBlockX());
            this.y2 = Math.max(corner1.getBlockY(), corner2.getBlockY());
            this.z2 = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        }

        public boolean contains(Location loc) {
            if (!Objects.equals(loc.getWorld(), world)) return false;
            int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
            return x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2;
        }
    }

    private static final Map<String, PvpZone> zones = new ConcurrentHashMap<>();

    private PvpZoneManager() {}

    /** ゾーンを登録する */
    public static void registerZone(PvpZone zone) {
        zones.put(zone.id, zone);
    }

    /** ゾーンを削除する */
    public static void unregisterZone(String id) {
        zones.remove(id);
    }

    /**
     * 指定Locationがいずれかのゾーン内かチェックする。
     * @return 含まれるゾーン（なければnull）
     */
    public static PvpZone getZone(Location location) {
        for (PvpZone zone : zones.values()) {
            if (zone.contains(location)) return zone;
        }
        return null;
    }

    /** 指定LocationがPvPゾーン内かどうか */
    public static boolean isInPvpZone(Location location) {
        return getZone(location) != null;
    }

    public static Collection<PvpZone> getAllZones() { return zones.values(); }

    public static int getZoneCount() { return zones.size(); }
}
