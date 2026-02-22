package com.woxloi.mythicrpg.party;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * パーティーチャット (!メッセージ でPTメンバーのみに送信)
 * および退出時の自動離脱
 */
public class PartyListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        if (!message.startsWith("!")) return;

        Player player = event.getPlayer();
        if (!RpgPartyManager.isInParty(player.getUniqueId())) return;

        event.setCancelled(true);
        String content = message.substring(1);
        Party party = RpgPartyManager.getParty(player.getUniqueId());
        if (party == null) return;

        RpgPartyManager.broadcast(party, "§e" + player.getName() + " §f» " + content);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (RpgPartyManager.isInParty(event.getPlayer().getUniqueId())) {
            RpgPartyManager.leave(event.getPlayer());
        }
    }
}
