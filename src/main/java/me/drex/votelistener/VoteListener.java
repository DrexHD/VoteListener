package me.drex.votelistener;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vexsoftware.votifier.model.Vote;
import me.drex.votelistener.command.Commands;
import me.drex.votelistener.config.ConfigManager;
import me.drex.votelistener.data.PlayerVoteData;
import me.drex.votelistener.data.VoteData;
import me.drex.votelistener.util.VotePlaceholders;
import me.drex.vanish.api.VanishAPI;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Util;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserNameToIdResolver;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class VoteListener implements DedicatedServerModInitializer {

    public static final String MOD_ID = "votelistener";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final boolean VANISH_PRESENT = FabricLoader.getInstance().isModLoaded("melius-vanish");

    public static final Codec<Vote> VOTE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("serviceName").forGetter(Vote::getServiceName),
        Codec.STRING.fieldOf("username").forGetter(Vote::getUsername),
        Codec.STRING.fieldOf("address").forGetter(Vote::getAddress),
        Codec.STRING.fieldOf("timeStamp").forGetter(Vote::getTimeStamp)
    ).apply(instance, Vote::new));

    private static MinecraftServer server;
    public static VoteData voteData;

    @Override
    public void onInitializeServer() {
        if (!ConfigManager.loadConfig()) {
            throw new IllegalStateException("Failed to load config, please fix your config file!");
        }
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            loadData(server);
            VoteListener.server = server;
        });
        ServerLifecycleEvents.AFTER_SAVE.register((server, flush, force) -> saveData(server));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> Commands.register(dispatcher));
        ServerTickEvents.START_SERVER_TICK.register(VoteListener::onTick);
        com.vexsoftware.votifier.fabric.event.VoteListener.EVENT.register(VoteListener::onVote);
        VotePlaceholders.register();
    }


    private static void onVote(Vote vote) {
        UserNameToIdResolver userNameToIdResolver = server.services().nameToIdCache();
        Util.nonCriticalIoPool().execute(() -> {
            Optional<NameAndId> optional = userNameToIdResolver.get(vote.getUsername());
            optional.ifPresentOrElse(
                nameAndId -> server.submit(() -> onVote(vote, nameAndId)),
                () -> LOGGER.info("Unknown player name \"{}\", discarding vote.", vote.getUsername())
            );
        });

    }

    private static void onVote(Vote vote, NameAndId nameAndId) {
        voteData.onVote(vote, nameAndId);
        for (String command : ConfigManager.CONFIG.commands) {
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), formatCommand(vote, nameAndId, command));
        }
    }

    private static void onTick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (VANISH_PRESENT && VanishAPI.isVanished(player)) continue;
            PlayerVoteData playerVoteData = voteData.players().get(player.getUUID());
            if (playerVoteData != null) {
                for (Vote vote : playerVoteData.unprocessedVotes()) {
                    for (String command : ConfigManager.CONFIG.onlineCommands) {
                        performCommand(server, player, vote, command);
                    }
                    int voteCount = playerVoteData.votes().indexOf(vote) + 1;
                    List<String> milestoneCommands = ConfigManager.CONFIG.milestones.get(voteCount);
                    if (milestoneCommands != null) {
                        for (String command : milestoneCommands) {
                            performCommand(server, player, vote, command);
                        }
                    }
                }
                playerVoteData.unprocessedVotes().clear();
            }
        }
    }

    private static void performCommand(MinecraftServer server, ServerPlayer player, Vote vote, String command) {
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withEntity(player).withSuppressedOutput(), formatCommand(vote, player.nameAndId(), command));
    }

    private static void loadData(MinecraftServer server) {
        Path path = server.getWorldPath(LevelResource.ROOT).resolve("votes.dat");
        if (Files.exists(path)) {
            try {
                CompoundTag compoundTag = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
                DataResult<VoteData> dataResult = VoteData.CODEC.parse(NbtOps.INSTANCE, compoundTag);
                voteData = dataResult.getOrThrow(RuntimeException::new);
            } catch (IOException e) {
                // Fail-fast
                throw new RuntimeException(e);
            }
        } else {
            voteData = new VoteData(new HashMap<>());
        }
    }

    public static void saveData(MinecraftServer server) {
        Path path = server.getWorldPath(LevelResource.ROOT).resolve("votes.dat");
        DataResult<Tag> dataResult = VoteData.CODEC.encodeStart(NbtOps.INSTANCE, voteData);
        Optional<Tag> optional = dataResult.resultOrPartial(error -> LOGGER.error("Failed to encode vote data, data will get lost: {}", error));
        optional.ifPresent(tag -> {
            try {
                NbtIo.writeCompressed((CompoundTag) tag, path);
            } catch (IOException e) {
                LOGGER.error("Failed to save vote data, data will get lost", e);
            }
        });
    }

    private static String formatCommand(Vote vote, NameAndId nameAndId, String command) {
        return command
            .replace("${uuid}", nameAndId.id().toString())
            .replace("${username}", nameAndId.name())
            .replace("${serviceName}", vote.getServiceName())
            .replace("${address}", vote.getAddress())
            .replace("${timeStamp}", vote.getTimeStamp());
    }
}