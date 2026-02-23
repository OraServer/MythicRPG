package com.woxloi.mythicrpg.dungeon;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 進行中ダンジョンの1セッション。
 * 参加者リスト・現在フロア・残り時間を保持する。
 */
public class DungeonSession {

    public enum State { WAITING, IN_PROGRESS, BOSS_FLOOR, COMPLETED, FAILED }

    private final String sessionId;
    private final DungeonDefinition definition;
    private final List<UUID> participants;
    private final long startTimeMs;

    private State state;
    private int currentFloor;
    private int remainingSeconds;

    /** 現在フロアで生存中のMob UUID セット（全滅判定に使用） */
    private Set<UUID> activeMobUuids = new HashSet<>();

    public DungeonSession(DungeonDefinition definition, List<Player> players) {
        this.sessionId      = UUID.randomUUID().toString().substring(0, 8);
        this.definition     = definition;
        this.participants   = new CopyOnWriteArrayList<>();
        this.state          = State.WAITING;
        this.currentFloor   = 0;
        this.remainingSeconds = definition.getTimeLimitSeconds();
        this.startTimeMs    = System.currentTimeMillis();

        players.forEach(p -> participants.add(p.getUniqueId()));
    }

    /** 次のフロアへ進む */
    public void advanceFloor() {
        currentFloor++;
        if (currentFloor >= definition.getFloorCount()) {
            state = State.BOSS_FLOOR;
        } else {
            state = State.IN_PROGRESS;
        }
    }

    /** ダンジョンクリア */
    public void complete() { state = State.COMPLETED; }

    /** ダンジョン失敗（全滅 or タイムアップ） */
    public void fail() { state = State.FAILED; }

    /** タイマーを1秒進める（BukkitTaskから毎秒呼ぶ） */
    public boolean tickTimer() {
        if (state != State.IN_PROGRESS && state != State.BOSS_FLOOR) return false;
        remainingSeconds--;
        return remainingSeconds <= 0; // true = タイムアップ
    }

    /** 参加者を追加 */
    public void addParticipant(UUID uuid) { participants.add(uuid); }

    /** 参加者を除外（死亡・ログアウト） */
    public void removeParticipant(UUID uuid) { participants.remove(uuid); }

    /** 全員離脱したか */
    public boolean isEmpty() { return participants.isEmpty(); }

    // ─── Getters ────────────────────────────────

    public String getSessionId()           { return sessionId; }
    public DungeonDefinition getDefinition() { return definition; }
    public List<UUID> getParticipants()    { return Collections.unmodifiableList(participants); }
    public State getState()                { return state; }
    public int getCurrentFloor()           { return currentFloor; }
    public int getRemainingSeconds()       { return remainingSeconds; }
    public long getStartTimeMs()           { return startTimeMs; }
    public boolean isFinished()            { return state == State.COMPLETED || state == State.FAILED; }

    /** 残り時間を mm:ss 形式で返す */
    public String getRemainingTimeDisplay() {
        int m = remainingSeconds / 60;
        int s = remainingSeconds % 60;
        String color = remainingSeconds <= 60 ? "§c" : remainingSeconds <= 180 ? "§e" : "§a";
        return color + String.format("%02d:%02d", m, s);
    }

    /** 進捗表示 */
    public String getProgressDisplay() {
        return "§7[§e" + currentFloor + "§7/§e" + definition.getFloorCount() + "§7]";
    }

    // ─── アクティブMob管理 ──────────────────────────────
    public void setActiveMobUuids(Set<UUID> uuids) { this.activeMobUuids = new HashSet<>(uuids); }
    public Set<UUID> getActiveMobUuids() { return activeMobUuids; }

    /**
     * Mobが死亡したことを通知する。全滅したらtrue。
     */
    public boolean onMobDied(UUID uuid) {
        activeMobUuids.remove(uuid);
        return activeMobUuids.isEmpty();
    }

    /** 全参加者にメッセージを送る */
    public void broadcastToParticipants(String msg) {
        for (UUID uuid : participants) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendMessage(com.woxloi.mythicrpg.MythicRPG.PREFIX + msg);
        }
    }
}
