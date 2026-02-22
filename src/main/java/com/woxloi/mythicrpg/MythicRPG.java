package com.woxloi.mythicrpg;

import com.woxloi.mythicrpg.core.PluginBootstrap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MythicRPG extends JavaPlugin {

    // =====================
    // Prefix / Message
    // =====================
    public static final String PREFIX = "§6[MythicRPG]§r ";

    /** プレフィックス付きメッセージをプレイヤーに送る */
    public static void playerPrefixMsg(Player player, String message) {
        player.sendMessage(PREFIX + message);
    }

    /** プレフィックス付きメッセージをCommandSenderに送る */
    public static void sendPrefixMsg(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + message);
    }

    // 後方互換ショートハンド（既存コードとの互換性のために残す）
    public static void msg(Player player, String message) { playerPrefixMsg(player, message); }
    public static void msg(CommandSender sender, String message) { sendPrefixMsg(sender, message); }

    // =====================
    // Instance
    // =====================
    private static MythicRPG instance;

    public static MythicRPG getInstance() {
        return instance;
    }

    // =====================
    // Lifecycle
    // =====================
    @Override
    public void onEnable() {
        instance = this;
        new PluginBootstrap(this).enable();
    }

    @Override
    public void onDisable() {
        new PluginBootstrap(this).disable();
    }
}
