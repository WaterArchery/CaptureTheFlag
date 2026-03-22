package me.waterarchery.ctf.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.waterarchery.ctf.CaptureTheFlag;
import me.waterarchery.ctf.configuration.ConfigFile;
import me.waterarchery.ctf.database.AbstractCtfDatabase;
import me.waterarchery.ctf.database.dialect.MySQLDialect;

import java.sql.Connection;
import java.sql.SQLException;

// it is much easier to use Hibernate or orm in general to handle but i am sadly avoiding heavy libraries
public class MySQLCtfDatabase extends AbstractCtfDatabase {

    private final HikariDataSource dataSource;

    public MySQLCtfDatabase() {
        this(CaptureTheFlag.getPluginConfig().getDatabase());
    }

    private MySQLCtfDatabase(ConfigFile.DatabaseConfiguration dbConfig) {
        super(dbConfig.getMaximumPoolSize(), new MySQLDialect());

        HikariConfig hikariConfig = createHikariConfig(dbConfig);
        dataSource = new HikariDataSource(hikariConfig);

        createTables();
    }

    @Override
    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void close() {
        super.close();

        try {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }
        } catch (Exception e) {
            logger.error("Failed to close database connection", e);
        }
    }

    private HikariConfig createHikariConfig(ConfigFile.DatabaseConfiguration dbConfig) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + dbConfig.getHost() + ":" + dbConfig.getPort() + "/" + dbConfig.getDatabase());
        hikariConfig.setUsername(dbConfig.getUsername());
        hikariConfig.setPassword(dbConfig.getPassword());
        hikariConfig.setMinimumIdle(dbConfig.getMinimumIdle());
        hikariConfig.setMaximumPoolSize(dbConfig.getMaximumPoolSize());
        hikariConfig.setConnectionTimeout(dbConfig.getConnectionTimeout());
        hikariConfig.setIdleTimeout(dbConfig.getIdleTimeout());
        hikariConfig.setMaxLifetime(dbConfig.getMaxLifetime());
        hikariConfig.setPoolName("CTF-HikariPool");
        return hikariConfig;
    }
}
