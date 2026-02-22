package com.woxloi.mythicrpg.party;

import com.woxloi.mythicrpg.quest.QuestPluginBridge;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MythicRPGのパーティー管理。
 * QuestPlugin導入時はQuestPluginのPartyManagerからメンバー情報を取得し、
 * 未導入時はMythicRPG独自の軽量パーティー機能を使う。
 */
public class MrpgPartyManager {

    /** QuestPlugin未導入時のフォールバック用パーティー管理 */
    private static final Map<UUID, PartyData> parties = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> playerToParty = new ConcurrentHashMap<>();  // メンバーUUID → リーダーUUID

    /* =====================
       メンバー取得（共通API）
     ===================== */

    /**
     * パーティーメンバーを取得する共通メソッド。
     * QuestPlugin有効時はそちらから取得、無効時はMythicRPG内部データを使う。
     */
    public static List<Player> getPartyMembers(Player player) {
        if (QuestPluginBridge.isAvailable()) {
            return QuestPluginBridge.getQuestPartyMembers(player);
        }
        return getLocalPartyMembers(player);
    }

    public static boolean isInParty(Player player) {
        if (QuestPluginBridge.isAvailable()) {
            return !QuestPluginBridge.getQuestPartyMembers(player).isEmpty()
                    && QuestPluginBridge.getQuestPartyMembers(player).size() > 1;
        }
        return playerToParty.containsKey(player.getUniqueId());
    }

    /* =====================
       フォールバック用（QuestPlugin未導入）
     ===================== */

    public static boolean createParty(Player leader) {
        if (playerToParty.containsKey(leader.getUniqueId())) return false;
        UUID leaderUUID = leader.getUniqueId();
        PartyData data = new PartyData(leaderUUID);
        parties.put(leaderUUID, data);
        playerToParty.put(leaderUUID, leaderUUID);
        leader.sendMessage("§a[Party] パーティーを作成しました");
        return true;
    }

    public static boolean joinParty(Player player, Player leader) {
        PartyData party = parties.get(leader.getUniqueId());
        if (party == null) return false;
        party.addMember(player.getUniqueId());
        playerToParty.put(player.getUniqueId(), leader.getUniqueId());
        player.sendMessage("§a[Party] " + leader.getName() + "のパーティーに参加しました");
        leader.sendMessage("§b[Party] " + player.getName() + "が参加しました");
        return true;
    }

    public static boolean leaveParty(Player player) {
        UUID leaderUUID = playerToParty.remove(player.getUniqueId());
        if (leaderUUID == null) return false;
        PartyData party = parties.get(leaderUUID);
        if (party != null) {
            party.removeMember(player.getUniqueId());
            // リーダーが抜けたら解散
            if (party.isLeader(player.getUniqueId())) {
                disbandParty(player);
            }
        }
        player.sendMessage("§e[Party] パーティーを離脱しました");
        return true;
    }

    public static boolean disbandParty(Player leader) {
        PartyData party = parties.remove(leader.getUniqueId());
        if (party == null) return false;
        for (UUID uuid : party.memberUUIDs) {
            playerToParty.remove(uuid);
            Player member = org.bukkit.Bukkit.getPlayer(uuid);
            if (member != null) member.sendMessage("§c[Party] パーティーが解散されました");
        }
        return true;
    }

    private static List<Player> getLocalPartyMembers(Player player) {
        UUID leaderUUID = playerToParty.get(player.getUniqueId());
        if (leaderUUID == null) return List.of();
        PartyData party = parties.get(leaderUUID);
        if (party == null) return List.of();
        return party.memberUUIDs.stream()
                .map(org.bukkit.Bukkit::getPlayer)
                .filter(p -> p != null && p.isOnline())
                .toList();
    }

    public static PartyData getPartyData(Player player) {
        UUID leaderUUID = playerToParty.get(player.getUniqueId());
        if (leaderUUID == null) return null;
        return parties.get(leaderUUID);
    }
}
