package me.drex.votelistener.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.drex.votelistener.VoteListener;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static me.drex.votelistener.VoteListener.MOD_ID;

public class ConfigManager {

    public static Config CONFIG = null;
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().setLenient().create();

    public static boolean loadConfig() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configFilePath = configDir.resolve("%s.json".formatted(MOD_ID));
        if (Files.notExists(configFilePath)) {
            CONFIG = new Config();
            String json = GSON.toJson(CONFIG);
            try {
                Files.writeString(configFilePath, json);
                return true;
            } catch (IOException e) {
                VoteListener.LOGGER.error("Failed to write default config...", e);
                return false;
            }
        }
        try {
            CONFIG = GSON.fromJson(Files.readString(configFilePath), Config.class);
            return true;
        } catch (IOException e) {
            VoteListener.LOGGER.error("Failed to load config, using default options", e);
            CONFIG = new Config();
            return false;
        }
    }


}
