package com.woxloi.mythicrpg.artifact;

import com.woxloi.mythicrpg.equipment.model.EquipSlot;

/**
 * アーティファクトセットを構成する1つの装備ピース。
 *
 * 各ピースには属するArtifactTypeとスロット情報が紐付いており、
 * RpgItemのメタデータ（PersistentDataContainer）にセット名を焼き込む。
 */
public class ArtifactPiece {

    private final ArtifactType setType;
    private final EquipSlot    slot;
    private final String       pieceName;    // 表示名 例: "竜殺しの剣"
    private final String       pieceId;      // 内部ID 例: "dragon_slayer_sword"

    // セットに依存しない固有ステータス
    private final int bonusAtk;
    private final int bonusDef;
    private final int bonusHp;
    private final int bonusMp;

    public ArtifactPiece(ArtifactType setType, EquipSlot slot,
                         String pieceId, String pieceName,
                         int bonusAtk, int bonusDef, int bonusHp, int bonusMp) {
        this.setType   = setType;
        this.slot      = slot;
        this.pieceId   = pieceId;
        this.pieceName = pieceName;
        this.bonusAtk  = bonusAtk;
        this.bonusDef  = bonusDef;
        this.bonusHp   = bonusHp;
        this.bonusMp   = bonusMp;
    }

    public ArtifactType getSetType()   { return setType; }
    public EquipSlot    getSlot()      { return slot; }
    public String       getPieceId()   { return pieceId; }
    public String       getPieceName() { return pieceName; }
    public int          getBonusAtk()  { return bonusAtk; }
    public int          getBonusDef()  { return bonusDef; }
    public int          getBonusHp()   { return bonusHp; }
    public int          getBonusMp()   { return bonusMp; }
}
