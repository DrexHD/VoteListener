package me.drex.votelistener.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vexsoftware.votifier.model.Vote;
import me.drex.votelistener.VoteListener;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

import static me.drex.votelistener.data.VoteData.TIME_COMPARATOR;

public final class PlayerVoteData {

    public static final Codec<PlayerVoteData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        VoteListener.VOTE_CODEC.listOf().fieldOf("votes").forGetter(PlayerVoteData::votes),
        VoteListener.VOTE_CODEC.listOf().fieldOf("unprocessedVotes").forGetter(PlayerVoteData::unprocessedVotes)
    ).apply(instance, (votes, unprocessedVotes) -> new PlayerVoteData(new LinkedList<>(votes), new LinkedList<>(unprocessedVotes))));
    // Player votes, ordered by timestamp
    private final List<Vote> votes;
    private final List<Vote> unprocessedVotes;

    public PlayerVoteData(List<Vote> votes, List<Vote> unprocessedVotes) {
        this.votes = votes;
        this.votes.sort(TIME_COMPARATOR);
        this.unprocessedVotes = unprocessedVotes;
    }

    public void onVote(Vote vote) {
        votes.add(vote);
        unprocessedVotes.add(vote);
    }

    public List<Vote> votesByTime(Duration duration) {
        return VoteData.binarySearchVotesByTime(this.votes, duration.toMillis());
    }

    public List<Vote> votes() {
        return votes;
    }

    public List<Vote> unprocessedVotes() {
        return unprocessedVotes;
    }

}
