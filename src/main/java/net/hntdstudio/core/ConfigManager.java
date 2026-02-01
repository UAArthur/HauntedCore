package net.hntdstudio.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.logger.HytaleLogger;
import lombok.Getter;
import net.hntdstudio.core.model.Config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {
    private final File configFile;
    @Getter
    private Config config;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    public ConfigManager(Main main) {
        configFile = new File(main.getDataDirectory() + "/config.json");
        this.loadConfig();
    }

    public void loadConfig() {
        if (!configFile.exists()) {
            config = new Config();
            saveConfig();
            HytaleLogger.forEnclosingClass().atInfo()
                    .log("No config found, created default config at %s", configFile.getPath());
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            config = GSON.fromJson(reader, Config.class);
            if (config == null) {
                config = new Config();
                saveConfig();
            }
        } catch (Exception e) {
            HytaleLogger.forEnclosingClass().atSevere()
                    .log("Error while trying to load config from %s: %s",
                            configFile.getName(), e.getMessage());
        }
    }

    public void saveConfig() {
        try {
            configFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            HytaleLogger.forEnclosingClass().atSevere()
                    .log("Error while trying to save config to %s: %s",
                            configFile.getName(), e.getMessage());
        }
    }
}