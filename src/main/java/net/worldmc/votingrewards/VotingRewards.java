package net.worldmc.votingrewards;

import net.worldmc.morpheus.Morpheus;
import net.worldmc.morpheus.api.MorpheusAPI;
import net.worldmc.votingrewards.database.Database;
import net.worldmc.votingrewards.listeners.VoteListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class VotingRewards extends JavaPlugin {
    private MorpheusAPI morpheusAPI;
    private Database database;

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("Morpheus") instanceof Morpheus)
            morpheusAPI = Morpheus.getAPI();

        saveDefaultConfig();

        database = new Database(this);
        database.connect();

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new VoteListener(this), this);
    }

    public Database getDatabase() {
        return database;
    }

    public MorpheusAPI getMorpheusAPI() {
        return morpheusAPI;
    }
}
