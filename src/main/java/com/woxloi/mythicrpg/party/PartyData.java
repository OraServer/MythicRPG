package com.woxloi.mythicrpg.party;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * MythicRPG側でのパーティー情報ホルダ。
 * パーティーシステムの実体はQuestPluginのPartyManagerに委譲するが、
 * MythicRPG独自の情報（EXP共有設定など）をここで管理する。
 */
public class PartyData {

    public final UUID leaderUUID;
    public final List<UUID> memberUUIDs = new ArrayList<>();

    /** EXP共有するか（デフォルトtrue） */
    public boolean shareExp = true;

    /** パーティーEXPボーナス倍率（メンバー数に応じて増加） */
    public double expBonus = 1.0;

    public PartyData(UUID leaderUUID) {
        this.leaderUUID = leaderUUID;
        this.memberUUIDs.add(leaderUUID);
    }

    /** メンバー追加 */
    public void addMember(UUID uuid) {
        if (!memberUUIDs.contains(uuid)) memberUUIDs.add(uuid);
        updateExpBonus();
    }

    /** メンバー削除 */
    public void removeMember(UUID uuid) {
        memberUUIDs.remove(uuid);
        updateExpBonus();
    }

    /** メンバー数に応じてEXPボーナスを更新 */
    private void updateExpBonus() {
        int size = memberUUIDs.size();
        // 2人: ×1.1, 3人: ×1.2, 4人: ×1.3, 5人以上: ×1.5
        expBonus = switch (size) {
            case 1 -> 1.0;
            case 2 -> 1.1;
            case 3 -> 1.2;
            case 4 -> 1.3;
            default -> 1.5;
        };
    }

    public int size() {
        return memberUUIDs.size();
    }

    public boolean isLeader(UUID uuid) {
        return leaderUUID.equals(uuid);
    }

    public boolean contains(UUID uuid) {
        return memberUUIDs.contains(uuid);
    }
}
