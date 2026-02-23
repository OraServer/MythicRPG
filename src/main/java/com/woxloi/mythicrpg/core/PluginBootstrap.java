package com.woxloi.mythicrpg.core;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.artifact.ArtifactListener;
import com.woxloi.mythicrpg.artifact.ArtifactManager;
import com.woxloi.mythicrpg.artifact.ArtifactRegistry;
import com.woxloi.mythicrpg.artifact.ArtifactRepository;
import com.woxloi.mythicrpg.buff.BuffListener;
import com.woxloi.mythicrpg.buff.BuffPotionListener;
import com.woxloi.mythicrpg.buff.BuffTickTask;
import com.woxloi.mythicrpg.combat.MobKillListener;
import com.woxloi.mythicrpg.combo.ComboDisplayTask;
import com.woxloi.mythicrpg.combo.ComboListener;
import com.woxloi.mythicrpg.command.MrpgCommand;
import com.woxloi.mythicrpg.db.DatabaseManager;
import com.woxloi.mythicrpg.db.TableInitializer;
import com.woxloi.mythicrpg.equipment.enchant.EnchantGUIListener;
import com.woxloi.mythicrpg.dungeon.DungeonGUI;
import com.woxloi.mythicrpg.dungeon.DungeonListener;
import com.woxloi.mythicrpg.element.ElementalDamageListener;
import com.woxloi.mythicrpg.element.ElementResistanceGUI;
import com.woxloi.mythicrpg.pet.PetListener;
import com.woxloi.mythicrpg.pvp.PvpListener;
import com.woxloi.mythicrpg.pvp.PvpRankingManager;
import com.woxloi.mythicrpg.ui.stats.StatDetailGUI;
import com.woxloi.mythicrpg.ui.title.TitleDetailGUI;
import com.woxloi.mythicrpg.equipment.drop.DropTableRegistry;
import com.woxloi.mythicrpg.combat.CombatListener;
import com.woxloi.mythicrpg.combat.MobDamageListener;
import com.woxloi.mythicrpg.equipment.drop.EquipDropListener;
import com.woxloi.mythicrpg.equipment.enhancer.EnhanceGUIListener;
import com.woxloi.mythicrpg.equipment.forge.ForgeGUIListener;
import com.woxloi.mythicrpg.equipment.listener.EquipmentGUIListener;
import com.woxloi.mythicrpg.equipment.identify.IdentifyListener;
import com.woxloi.mythicrpg.equipment.refine.RefineGUIListener;
import com.woxloi.mythicrpg.equipment.socket.SocketGUIListener;
import com.woxloi.mythicrpg.equipment.transfer.TransferGUIListener;
import com.woxloi.mythicrpg.stats.StatGUI;
import com.woxloi.mythicrpg.title.TitleGUI;
import com.woxloi.mythicrpg.ui.ProfileGUI;
import com.woxloi.mythicrpg.job.JobListener;
import com.woxloi.mythicrpg.party.PartyListener;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import com.woxloi.mythicrpg.player.PlayerListener;
import com.woxloi.mythicrpg.quest.QuestCompleteListener;
import com.woxloi.mythicrpg.quest.QuestPluginBridge;
import com.woxloi.mythicrpg.skill.WeaponSkillListener;
import com.woxloi.mythicrpg.skill.loader.SkillLoader;
import com.woxloi.mythicrpg.title.TitleListener;
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

        // 1. DB接続 & テーブル初期化
        initDatabase();

        // 1.5. Toggle設定読み込み
        com.woxloi.mythicrpg.core.PluginToggleManager.init();

        // 2. スキルYAMLロード
        SkillLoader.load();

        // 3. 装備レジストリ初期化（items/*.yml + デフォルト装備）
        com.woxloi.mythicrpg.equipment.EquipmentRegistry.init(plugin.getDataFolder());

        // 4. アーティファクト初期化（YAML読み込み → Registry → NBTキー）
        ArtifactManager.init();
        ArtifactRegistry.load();
        ArtifactRepository.createTable();

        // 4. ドロップテーブル初期化
        DropTableRegistry.load();

        // 5. QuestPlugin連携チェック
        QuestPluginBridge.init();

        // 5. コマンド・リスナー・タスク
        registerCommands();
        registerListeners();
        startTasks();

        MythicLogger.info("Enabled v" + plugin.getDescription().getVersion());
    }

    /* =====================
       Disable
     ===================== */
    public void disable() {
        stopTasks();

        // オンラインプレイヤーのデータを同期保存
        MythicLogger.info("全プレイヤーデータを保存中...");
        PlayerDataManager.saveAll();

        // DB接続を閉じる
        DatabaseManager.shutdown();

        MythicLogger.info("Disabled");
    }

    /* =====================
       DB
     ===================== */
    private void initDatabase() {
        try {
            DatabaseManager.init();
            TableInitializer.createTables();
        } catch (Exception e) {
            MythicLogger.error("DB初期化失敗: " + e.getMessage());
            MythicLogger.error("config.yml の database 設定を確認してください");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    /* =====================
       Commands
     ===================== */
    private void registerCommands() {
        MrpgCommand cmd = new MrpgCommand();
        plugin.getCommand("mrpg").setExecutor(cmd);
        plugin.getCommand("mrpg").setTabCompleter(cmd);
        MythicLogger.debug("Commands registered");
    }

    /* =====================
       Listeners
     ===================== */
    private void registerListeners() {
        // コア
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new SkillClickListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new JobListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new WeaponSkillListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new MobKillListener(), plugin);

        // 戦闘HP同期（最重要: バニラダメージ→PlayerData反映）
        Bukkit.getPluginManager().registerEvents(new CombatListener(), plugin);

        // vs Mob RPGダメージ計算（ATK/DEF/クリティカル/コンボ倍率）
        Bukkit.getPluginManager().registerEvents(new MobDamageListener(), plugin);

        // アーティファクト
        Bukkit.getPluginManager().registerEvents(new ArtifactListener(), plugin);

        // バフ/デバフ
        Bukkit.getPluginManager().registerEvents(new BuffListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new BuffPotionListener(), plugin);

        // 称号
        Bukkit.getPluginManager().registerEvents(new TitleListener(), plugin);

        // コンボ
        Bukkit.getPluginManager().registerEvents(new ComboListener(), plugin);

        // パーティー
        Bukkit.getPluginManager().registerEvents(new PartyListener(), plugin);

        // 装備GUI（未登録だったもの）
        Bukkit.getPluginManager().registerEvents(new EquipmentGUIListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new EnhanceGUIListener(), plugin);

        // 装備拡張システム
        Bukkit.getPluginManager().registerEvents(new EquipDropListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new IdentifyListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new SocketGUIListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new RefineGUIListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new TransferGUIListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new ForgeGUIListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new EnchantGUIListener(), plugin);

        // 属性システム
        Bukkit.getPluginManager().registerEvents(new ElementalDamageListener(), plugin);
        Bukkit.getPluginManager().registerEvents(ElementResistanceGUI.INSTANCE, plugin);

        // ダンジョン
        Bukkit.getPluginManager().registerEvents(new DungeonListener(), plugin);
        Bukkit.getPluginManager().registerEvents(DungeonGUI.INSTANCE, plugin);

        // ペット
        Bukkit.getPluginManager().registerEvents(new PetListener(), plugin);

        // PvP
        Bukkit.getPluginManager().registerEvents(new PvpListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PvpRankingManager(), plugin);

        // UI（未登録だったもの）
        Bukkit.getPluginManager().registerEvents(new StatGUI(), plugin);
        Bukkit.getPluginManager().registerEvents(new TitleGUI(), plugin);
        Bukkit.getPluginManager().registerEvents(new ProfileGUI(), plugin);
        Bukkit.getPluginManager().registerEvents(StatDetailGUI.INSTANCE, plugin);
        Bukkit.getPluginManager().registerEvents(TitleDetailGUI.INSTANCE, plugin);

        // QuestPlugin連携 (QuestPluginが存在する場合のみ有効)
        if (QuestPluginBridge.isAvailable()) {
            Bukkit.getPluginManager().registerEvents(new QuestCompleteListener(), plugin);
            MythicLogger.info("QuestPlugin連携リスナーを登録しました");
        }

        MythicLogger.debug("Listeners registered");
    }

    /* =====================
       Tasks
     ===================== */
    private void startTasks() {
        ScoreboardTask.start();
        ActionBarTask.start();
        ResourceRegenTask.start();
        BuffTickTask.start();
        ComboDisplayTask.start();
        MythicLogger.debug("Tasks started");
    }

    private void stopTasks() {
        ScoreboardTask.stop();
        ActionBarTask.stop();
        ResourceRegenTask.stop();
        BuffTickTask.stop();
        ComboDisplayTask.stop();
        MythicLogger.debug("Tasks stopped");
    }
}
