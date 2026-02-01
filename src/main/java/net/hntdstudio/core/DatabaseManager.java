package net.hntdstudio.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.pool.HikariPool;
import net.hntdstudio.core.model.Config;

public class DatabaseManager {
    private final Main main;
    private final HikariPool pool;

    public DatabaseManager(Main main) {
        this.main = main;
        try {
            Class.forName("net.hntdstudio.shadow.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL driver not found", e);
        }
        pool = setupConnection(main.getConfigManager().getConfig().getMysql());
    }

    private HikariPool setupConnection(Config.MySQL mysqlConfig) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(mysqlConfig.getUrl());
        config.setUsername(mysqlConfig.getUsername());
        config.setPassword(mysqlConfig.getPassword());
        config.setMaximumPoolSize(10);
        return new HikariPool(config);
    }
}