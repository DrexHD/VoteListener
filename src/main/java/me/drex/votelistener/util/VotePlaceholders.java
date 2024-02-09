package me.drex.votelistener.util;

import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import me.drex.votelistener.VoteListener;
import me.drex.votelistener.data.PlayerVoteData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import static me.drex.votelistener.VoteListener.MOD_ID;

public class VotePlaceholders {

    private static final ResourceLocation VOTE_COUNT = new ResourceLocation(MOD_ID, "vote_count");

    public static void register() {
        Placeholders.register(VOTE_COUNT, (context, argument) -> {
            ServerPlayer player = context.player();
            if (player != null) {
                PlayerVoteData playerVoteData = VoteListener.voteData.players().get(player.getUUID());
                int count = 0;
                if (playerVoteData != null) {
                    count = playerVoteData.votes().size();
                }
                return PlaceholderResult.value(String.valueOf(count));
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });
    }

}
