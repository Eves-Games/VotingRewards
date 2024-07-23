package net.worldmc.votingrewards.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

public class MessageUtil {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static Component formatMessage(String message, Player player) {
        TagResolver.Builder tagResolverBuilder = TagResolver.builder()
                .resolver(Placeholder.parsed("player", player.getName()));

        return miniMessage.deserialize(message, tagResolverBuilder.build());
    }

    public static Component formatMessage(String message, Player player, int offlineVotes) {
        TagResolver.Builder tagResolverBuilder = TagResolver.builder()
                .resolver(Placeholder.parsed("player", player.getName()))
                .resolver(Placeholder.parsed("offline_votes", String.valueOf(offlineVotes)));

        return miniMessage.deserialize(message, tagResolverBuilder.build());
    }
}