package com.woxloi.mythicrpg.core;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.combat.MobKillListener;
import com.woxloi.mythicrpg.job.JobListener;
import com.woxloi.mythicrpg.player.PlayerListener;
import com.woxloi.mythicrpg.skill.WeaponSkillListener;
import com.woxloi.mythicrpg.ui.ActionBarTask;
import com.woxloi.mythicrpg.ui.ResourceRegenTask;
import com.woxloi.mythicrpg.ui.ScoreboardTask;
import com.woxloi.mythicrpg.ui.skill.SkillClickListener;
import org.bukkit.Bukkit;

public class PluginBootstrap {

    private final MythicRPG plugin;

    public PluginBootstrap(MythicRPG plugin) {
        this.plugin = plugin;
    }

    /* =====================
       Enable
     ===================== */
    public void enable() {
        plugin.saveDefaultConfig();

        registerListeners();
        startTasks();

        MythicLogger.info("MythicRPG Enabled");
    }

    /* =====================
       Disable
     ===================== */
    public void disable() {
        stopTasks();

        MythicLogger.info("MythicRPG Disabled");
    }

    /* =====================
       Listener
     ===================== */
    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new SkillClickListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new JobListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new WeaponSkillListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new MobKillListener(), plugin);

        MythicLogger.debug("Listeners registered");
    }

    /* =====================
       Task
     ===================== */
    private void startTasks() {
        ScoreboardTask.start();
        ActionBarTask.start();
        ResourceRegenTask.start();

        MythicLogger.debug("Tasks started");
    }

    private void stopTasks() {
        ScoreboardTask.stop();
        ActionBarTask.stop();
        ResourceRegenTask.stop();

        MythicLogger.debug("Tasks stopped");
    }
}
