package com.woxloi.mythicrpg.equipment.socket;

import org.bukkit.ChatColor;

/**
 * 装備アイテムの1つのソケットスロット。
 * 宝石が嵌まっているかどうかを管理する。
 */
public class SocketSlot {

    private GemType gem; // null = 空スロット

    public SocketSlot() {
        this.gem = null;
    }

    public SocketSlot(GemType gem) {
        this.gem = gem;
    }

    public boolean isEmpty() {
        return gem == null;
    }

    public GemType getGem() {
        return gem;
    }

    public void setGem(GemType gem) {
        this.gem = gem;
    }

    public void removeGem() {
        this.gem = null;
    }

    /**
     * Lore表示用の文字列を返す。
     */
    public String toDisplayString() {
        if (isEmpty()) {
            return ChatColor.GRAY + "[ 空きソケット ]";
        }
        return ChatColor.GRAY + "[" + gem.getColoredName() + ChatColor.GRAY + "]";
    }

    @Override
    public String toString() {
        return isEmpty() ? "EMPTY" : gem.name();
    }
}
