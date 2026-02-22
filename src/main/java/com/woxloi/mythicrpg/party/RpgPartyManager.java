package com.woxloi.mythicrpg.party;

import com.woxloi.mythicrpg.buff.BuffManager;
import com.woxloi.mythicrpg.buff.BuffType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * MythicRPG側のパーティー管理
 * QuestPlugin の PartyManager を橋渡しし、
 * RPG固有機能 (バフ共有・EXP分配) を提供する
 */
public class RpgPartyManager {

    // UUID → Party
    private static final Map<UUID, UUID>   memberToParty = new HashMap<>(); // uuid → partyId
    private static final Map<UUID, Party>  parties       = new HashMap<>(); // partyId → Party

    // ─── 作成・解散 ───

    public static Party create(Player leader) {
        if (isInParty(leader.getUniqueId())) return null;
        Party party = new Party(leader.getUniqueId(), leader.getName());
        parties.put(party.getPartyId(), party);
        memberToParty.put(leader.getUniqueId(), party.getPartyId());
        return party;
    }

    public static void disband(UUID partyId) {
        Party party = parties.remove(partyId);
        if (party == null) return;
        party.getMemberUuids().forEach(memberToParty::remove);
    }

    // ─── 参加・退出 ───

    public static boolean join(Player player, UUID partyId) {
        Party party = parties.get(partyId);
        if (party == null || party.isFull() || isInParty(player.getUniqueId())) return false;
        party.addMember(player.getUniqueId(), player.getName());
        memberToParty.put(player.getUniqueId(), partyId);
        broadcast(party, "§a" + player.getName() + " がパーティーに参加しました");
        return true;
    }

    public static void leave(Player player) {
        UUID partyId = memberToParty.remove(player.getUniqueId());
        if (partyId == null) return;
        Party party = parties.get(partyId);
        if (party == null) return;
        party.removeMember(player.getUniqueId());
        broadcast(party, "§e" + player.getName() + " がパーティーを離脱しました");
        if (party.isEmpty()) parties.remove(partyId);
    }

    // ─── 取得 ───

    public static boolean isInParty(UUID uuid) { return memberToParty.containsKey(uuid); }

    public static Party getParty(UUID uuid) {
        UUID partyId = memberToParty.get(uuid);
        return partyId == null ? null : parties.get(partyId);
    }

    public static List<Player> getOnlineMembers(UUID uuid) {
        Party party = getParty(uuid);
        if (party == null) return List.of();
        List<Player> result = new ArrayList<>();
        for (UUID id : party.getMemberUuids()) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) result.add(p);
        }
        return result;
    }

    // ─── EXP 分配 ───
    /**
     * パーティーメンバー間でEXPを分配 (討伐報酬など)
     * @param uuid 討伐したプレイヤー
     * @param totalExp 分配前の合計EXP
     */
    public static void shareExp(UUID uuid, double totalExp) {
        Party party = getParty(uuid);
        if (party == null) return;
        List<Player> members = getOnlineMembers(uuid);
        if (members.size() <= 1) return;
        double shared = totalExp * 0.7 / members.size(); // 30%ペナルティ
        for (Player member : members) {
            if (!member.getUniqueId().equals(uuid)) {
                com.woxloi.mythicrpg.player.PlayerDataManager.get(member.getUniqueId());
                member.sendMessage("§e[PT] EXP +" + (int) shared);
            }
        }
    }

    // ─── バフ共有 ───
    /**
     * パーティー全員に同じバフを付与
     */
    public static void shareBuffToParty(Player caster, BuffType type, double magnitude, int durationTicks) {
        List<Player> members = getOnlineMembers(caster.getUniqueId());
        for (Player member : members) {
            BuffManager.applyBuff(member, type, magnitude, durationTicks, "party_share");
        }
    }

    // ─── ブロードキャスト ───
    public static void broadcast(Party party, String message) {
        for (UUID id : party.getMemberUuids()) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) p.sendMessage("§b[PT] §f" + message);
        }
    }
}
