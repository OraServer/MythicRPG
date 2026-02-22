package com.woxloi.mythicrpg.party;

import java.util.*;

/**
 * RPGパーティー (MythicRPG側 - QuestPluginのPartyManagerとは別に独立管理)
 * QuestPlugin の PartyManager をブリッジ利用するため、
 * このクラスはステータス共有・バフ共有などRPG固有の処理を担う
 */
public class Party {

    private final UUID partyId;
    private UUID leaderUuid;
    private final Map<UUID, PartyMember> members = new LinkedHashMap<>();
    private static final int MAX_SIZE = 4;

    public Party(UUID leaderUuid, String leaderName) {
        this.partyId    = UUID.randomUUID();
        this.leaderUuid = leaderUuid;
        members.put(leaderUuid, new PartyMember(leaderUuid, leaderName, true));
    }

    // ─── メンバー操作 ───

    public boolean addMember(UUID uuid, String name) {
        if (members.size() >= MAX_SIZE) return false;
        if (members.containsKey(uuid)) return false;
        members.put(uuid, new PartyMember(uuid, name, false));
        return true;
    }

    public boolean removeMember(UUID uuid) {
        if (!members.containsKey(uuid)) return false;
        members.remove(uuid);
        // リーダーが抜けた場合は次のメンバーに委譲
        if (uuid.equals(leaderUuid) && !members.isEmpty()) {
            leaderUuid = members.keySet().iterator().next();
            members.get(leaderUuid).setLeader(true);
        }
        return true;
    }

    // ─── 取得 ───

    public UUID getPartyId()              { return partyId; }
    public UUID getLeaderUuid()           { return leaderUuid; }
    public Collection<PartyMember> getMembers() { return members.values(); }
    public Set<UUID> getMemberUuids()     { return members.keySet(); }
    public int getSize()                  { return members.size(); }
    public boolean isFull()              { return members.size() >= MAX_SIZE; }
    public boolean isLeader(UUID uuid)   { return uuid.equals(leaderUuid); }
    public boolean hasMember(UUID uuid)  { return members.containsKey(uuid); }
    public PartyMember getMember(UUID uuid) { return members.get(uuid); }
    public boolean isEmpty()             { return members.isEmpty(); }
}
