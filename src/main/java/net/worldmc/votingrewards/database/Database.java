package net.worldmc.votingrewards.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.worldmc.votingrewards.VotingRewards;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;

public class Database {
    private HikariDataSource dataSource;
    private final VotingRewards plugin;

    public Database(VotingRewards plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        FileConfiguration config = plugin.getConfig();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getString("database.jdbc-url", "jdbc:mysql://localhost:3306/database"));
        hikariConfig.setUsername(config.getString("database.username", "username"));
        hikariConfig.setPassword(config.getString("database.password", "password"));

        dataSource = new HikariDataSource(hikariConfig);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}