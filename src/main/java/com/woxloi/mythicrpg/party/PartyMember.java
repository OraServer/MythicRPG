package com.woxloi.mythicrpg.party;

import java.util.UUID;

/**
 * パーティーメンバー1人分のデータ
 */
public class PartyMember {

    private final UUID uuid;
    private String name;
    private boolean isLeader;
    private long joinedAt;

    public PartyMember(UUID uuid, String name, boolean isLeader) {
        this.uuid     = uuid;
        this.name     = name;
        this.isLeader = isLeader;
        this.joinedAt = System.currentTimeMillis();
    }

    public UUID getUuid()       { return uuid; }
    public String getName()     { return name; }
    public boolean isLeader()   { return isLeader; }
    public long getJoinedAt()   { return joinedAt; }

    public void setLeader(boolean leader) { isLeader = leader; }
    public void setName(String name)      { this.name = name; }
}
