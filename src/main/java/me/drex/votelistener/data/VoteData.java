package me.drex.votelistener.data;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vexsoftware.votifier.model.Vote;
import me.drex.votelistener.VoteListener;
import net.minecraft.core.UUIDUtil;

import java.time.Duration;
import java.util.*;

public final class VoteData {

    public static final Codec<VoteData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.unboundedMap(UUIDUtil.STRING_CODEC, PlayerVoteData.CODEC).fieldOf("players").forGetter(VoteData::players)
    ).apply(instance, players -> new VoteData(new HashMap<>(players))));
    public static final Comparator<Vote> TIME_COMPARATOR = Comparator.comparing(VoteData::getTimeStamp);

    private final Map<UUID, PlayerVoteData> players;
    // All votes, ordered by timestamp
    private final ArrayList<Vote> votesByTime = new ArrayList<>();

    public VoteData(Map<UUID, PlayerVoteData> players) {
        this.players = players;
        players.forEach((key, value) -> votesByTime.addAll(value.votes()));
        this.votesByTime.sort(TIME_COMPARATOR);
    }

    public void onVote(Vote vote, GameProfile profile) {
        PlayerVoteData playerVoteData = players.computeIfAbsent(profile.getId(), (profile_) -> new PlayerVoteData(new LinkedList<>(), new LinkedList<>()));
        playerVoteData.onVote(vote);
        this.votesByTime.add(vote);
    }

    public Map<UUID, PlayerVoteData> players() {
        return players;
    }

    static List<Vote> binarySearchVotesByTime(List<Vote> votesByTime, long minUnixMS) {
        // O(log(n))
        int low = 0, high = votesByTime.size();
        int mid;

        while (low < high) {

            mid = low + (high - low) / 2;
            if (minUnixMS <= getTimeStamp(votesByTime.get(mid))) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }

        if (low < votesByTime.size() && getTimeStamp(votesByTime.get(low)) < minUnixMS) {
            low++;
        }

        return votesByTime.subList(low, votesByTime.size());
    }

    public List<Vote> votesByTime(Duration duration) {
        return binarySearchVotesByTime(this.votesByTime, duration.toMillis());
    }

    public static long getTimeStamp(Vote vote) {
        String timeStamp = vote.getTimeStamp();
        try {
            return Long.parseLong(timeStamp);
        } catch (NumberFormatException e) {
            VoteListener.LOGGER.error("Failed to convert vote timestamp", e);
            return 0L;
        }
    }

}
