package haage.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

public final class LocatorHeadsConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "locator-heads.json";

    private LocatorHeadsConfigManager() {
    }

    public static LocatorHeadsConfig load(Logger logger) {
        Path path = getConfigPath();
        LocatorHeadsConfig defaults = new LocatorHeadsConfig();

        if (!Files.exists(path)) {
            save(defaults, logger);
            logger.info("Created default config at {}", path);
            return defaults;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            LocatorHeadsConfig loaded = GSON.fromJson(reader, LocatorHeadsConfig.class);
            if (loaded == null) {
                logger.warn("Config file was empty or invalid JSON. Using defaults.");
                save(defaults, logger);
                return defaults;
            }
            return loaded;
        } catch (Exception e) {
            logger.warn("Failed to read config file {}. Using defaults.", path, e);
            save(defaults, logger);
            return defaults;
        }
    }

    public static void save(LocatorHeadsConfig config, Logger logger) {
        Path path = getConfigPath();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            logger.error("Failed to write config file {}", path, e);
        }
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }
}