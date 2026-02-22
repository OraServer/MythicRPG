package com.woxloi.mythicrpg.db;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.core.MythicLogger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * HikariCPを使ったMySQL接続プール管理クラス。
 * PluginBootstrap から enable/disable を呼ぶ。
 */
public class DatabaseManager {

    private static HikariDataSource dataSource;

    /* =====================
       初期化
     ===================== */
    public static void init() {
        var cfg = MythicRPG.getInstance().getConfig();

        String host     = cfg.getString("database.host", "localhost");
        int    port     = cfg.getInt("database.port", 3306);
        String name     = cfg.getString("database.name", "mythicrpg");
        String user     = cfg.getString("database.user", "root");
        String password = cfg.getString("database.password", "password");
        int    poolSize = cfg.getInt("database.pool-size", 10);
        long   timeout  = cfg.getLong("database.connection-timeout", 30000);

        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + name
                + "?useSSL=false&autoReconnect=true&characterEncoding=UTF-8");
        hikari.setUsername(user);
        hikari.setPassword(password);
        hikari.setMaximumPoolSize(poolSize);
        hikari.setConnectionTimeout(timeout);
        hikari.setPoolName("MythicRPG-Pool");

        // MySQL向け推奨設定
        hikari.addDataSourceProperty("cachePrepStmts", "true");
        hikari.addDataSourceProperty("prepStmtCacheSize", "250");
        hikari.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(hikari);
        MythicLogger.info("MySQL接続プール初期化完了 (" + host + ":" + port + "/" + name + ")");
    }

    /* =====================
       接続取得
     ===================== */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DatabaseManager が初期化されていません");
        }
        return dataSource.getConnection();
    }

    /* =====================
       シャットダウン
     ===================== */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            MythicLogger.info("MySQL接続プールをクローズしました");
        }
    }

    public static boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }
}
