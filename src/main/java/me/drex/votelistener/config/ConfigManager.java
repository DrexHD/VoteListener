package me.drex.votelistener.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static me.drex.votelistener.VoteListener.LOGGER;

public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().setLenient().create();
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("votelistener.json");
    public static Config config = new Config();

    public static boolean load() {
        LOGGER.info("Loading votelistener config");
        if (Files.exists(CONFIG_FILE)) {
            try {
                String data = Files.readString(CONFIG_FILE);
                try {
                    config = GSON.fromJson(data, Config.class);
                    Files.writeString(CONFIG_FILE, GSON.toJson(config));
                    return true;
                } catch (JsonSyntaxException e) {
                    LOGGER.error("Failed to parse votelistener config", e);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load votelistener config", e);
            }
        } else {
            try {
                Files.writeString(CONFIG_FILE, GSON.toJson(config));
                return true;
            } catch (IOException e) {
                LOGGER.error("Failed to save votelistener config", e);
            }
        }
        return false;
    }

}
