package com.zeide.lapitxohara.config;

import com.zeide.lapitxohara.LapitxOharaMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

public class LapitxOharaConfig {
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("lapitx-ohara.properties").toFile();

    private final Properties properties = new Properties();

    private LapitxOharaConfig() {}

    public void writeConfig() {
        File configDir = CONFIG_FILE.getParentFile();
        if (!configDir.exists()) {
            if (!configDir.mkdirs()) {
                LapitxOharaMod.LOGGER.error("Unable to create config directory");
                return;
            }
        }

        if (!CONFIG_FILE.exists()) {
            try {
                if (!CONFIG_FILE.createNewFile()) {
                    LapitxOharaMod.LOGGER.error("Unable to create config file");
                    return;
                }
            } catch (IOException e) {
                LapitxOharaMod.LOGGER.error("Unable to create config file");
                return;
            }
        }

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            properties.store(out, "LapitxOhara config and data file");
        } catch (IOException e) {
            LapitxOharaMod.LOGGER.error("Unable to write config file");
        }
    }

    public Optional<SpawnPointData> getSpawnPointData() {
        try {
            Identifier dimensionIdentifier = Identifier.tryParse(properties.getProperty("spawnPointData.dimension"));
            if (dimensionIdentifier == null) {
                LapitxOharaMod.LOGGER.warn("Invalid spawn point dimension identifier in config file");
                return Optional.empty();
            }

            RegistryKey<World> dimension = RegistryKey.of(Registry.WORLD_KEY, dimensionIdentifier);

            double x = Double.parseDouble(properties.getProperty("spawnPointData.x"));
            double y = Double.parseDouble(properties.getProperty("spawnPointData.y"));
            double z = Double.parseDouble(properties.getProperty("spawnPointData.z"));
            float yaw = Float.parseFloat(properties.getProperty("spawnPointData.yaw"));
            float pitch = Float.parseFloat(properties.getProperty("spawnPointData.pitch"));

            return Optional.of(new SpawnPointData(dimension, x, y, z, yaw, pitch));
        } catch (NumberFormatException e) {
            LapitxOharaMod.LOGGER.warn("Invalid spawn point numbers in config file");
        } catch (NullPointerException e) {
            LapitxOharaMod.LOGGER.warn("Null spawn point values in config file");
        }

        return Optional.empty();
    }

    public void setSpawnPointData(SpawnPointData spawnPointData) {
        if (spawnPointData == null) {
            properties.remove("spawnPointData.dimension");
            properties.remove("spawnPointData.x");
            properties.remove("spawnPointData.y");
            properties.remove("spawnPointData.z");
            properties.remove("spawnPointData.yaw");
            properties.remove("spawnPointData.pitch");
            return;
        }

        properties.put("spawnPointData.dimension", spawnPointData.dimension().getValue().toString());
        properties.put("spawnPointData.x", Double.toString(spawnPointData.x()));
        properties.put("spawnPointData.y", Double.toString(spawnPointData.y()));
        properties.put("spawnPointData.z", Double.toString(spawnPointData.z()));
        properties.put("spawnPointData.yaw", Float.toString(spawnPointData.yaw()));
        properties.put("spawnPointData.pitch", Float.toString(spawnPointData.pitch()));
    }

    public static LapitxOharaConfig loadConfig() {
        LapitxOharaConfig config = new LapitxOharaConfig();
        if (!CONFIG_FILE.exists()) {
            config.writeConfig();
            return config;
        }

        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            config.properties.load(in);
        } catch (IOException e) {
            LapitxOharaMod.LOGGER.error("Unable to read config file");
        }

        return config;
    }
}
