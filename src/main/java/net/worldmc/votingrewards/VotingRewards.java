package net.worldmc.votingrewards;

import net.worldmc.votingrewards.database.Database;
import net.worldmc.votingrewards.listeners.VoteListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class VotingRewards extends JavaPlugin {
    private Database database;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        database = new Database(this);
        database.connect();

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new VoteListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public Database getDatabase() {
        return database;
    }
}
