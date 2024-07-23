package net.worldmc.votingrewards.listeners;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import net.worldmc.votingrewards.VotingRewards;
import net.worldmc.votingrewards.database.Database;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.*;
import java.util.UUID;

import static net.worldmc.votingrewards.util.MessageUtil.formatMessage;

public class VoteListener implements Listener {
    private final VotingRewards plugin;
    private final Database database;

    public VoteListener(VotingRewards votingRewards) {
        this.plugin = votingRewards;
        this.database = votingRewards.getDatabase();
        createTableIfNotExists();
    }

    @EventHandler
    public void onVote(VotifierEvent event) {
        Vote vote = event.getVote();
        OfflinePlayer player = Bukkit.getOfflinePlayer(vote.getUsername());

        handleVote(player.getUniqueId(), player.isOnline());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int offlineVotes = getOfflineVotes(player.getUniqueId());

        if (offlineVotes > 0) {
            for (int i = 0; i < offlineVotes; i++) {
                giveReward(player);
            }
            clearOfflineVotes(player.getUniqueId());
            String offlineVotesMessage = plugin.getConfig().getString("voting.offline-votes-message", "<gray>[<b><gradient:#00AA00:#FFAA00>WorldMC</gradient></b>] <green>You received <yellow><offline_votes></yellow> vote crates while you were offline!");
            player.sendMessage(formatMessage(offlineVotesMessage, player, offlineVotes));
        }
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS player_votes (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "total_votes INT NOT NULL DEFAULT 0," +
                "offline_votes INT NOT NULL DEFAULT 0" +
                ")";

        try (Connection conn = database.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create player_votes table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void handleVote(UUID uuid, boolean isOnline) {
        String sql = "INSERT INTO player_votes (uuid, total_votes, offline_votes) VALUES (?, 1, ?) " +
                "ON DUPLICATE KEY UPDATE total_votes = total_votes + 1, offline_votes = offline_votes + ?";

        try (Connection conn = database.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setInt(2, isOnline ? 0 : 1);
            preparedStatement.setInt(3, isOnline ? 0 : 1);
            preparedStatement.executeUpdate();

            if (isOnline) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    giveReward(player);
                }
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to handle vote: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int getOfflineVotes(UUID uuid) {
        String sql = "SELECT offline_votes FROM player_votes WHERE uuid = ?";

        try (Connection conn = database.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setString(1, uuid.toString());
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return rs.getInt("offline_votes");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get offline votes: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    private void clearOfflineVotes(UUID uuid) {
        String sql = "UPDATE player_votes SET offline_votes = 0 WHERE uuid = ?";

        try (Connection conn = database.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setString(1, uuid.toString());
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to clear offline votes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void giveReward(Player player) {
        String command = plugin.getConfig().getString("voting.command", "crates give <player> vote");
        command = command.replace("<player>", player.getName());

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

        String soundName = plugin.getConfig().getString("voting.default-sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
        player.playSound(player.getLocation(), Sound.valueOf(soundName), 1.0f, 1.0f);

        String message = plugin.getConfig().getString("voting.message", "<gray>[<b><gradient:#00AA00:#FFAA00>WorldMC</gradient></b>] <green>Thanks for voting, <player>! You've received <yellow>1 vote crate.</yellow>");
        player.sendMessage(formatMessage(message, player));
    }
}