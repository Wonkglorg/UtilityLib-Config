package com.wonkglorg.utilitylib.config;

import com.wonkglorg.utilitylib.config.types.Config;
import com.wonkglorg.utilitylib.config.types.ConfigYML;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Configmanager to handle accessing configs.
 *
 * <br>
 * <br>
 * BEFORE USING THIS CLASS MAKE SURE TO CALL {@link ConfigManager#createInstance(JavaPlugin)} TO INITIALIZE THE INSTANCE
 *
 * @author Wonkglorg
 */
@SuppressWarnings("unused")
public final class ConfigManager {
    /**
     * The Logger instance
     */
    private final Logger LOGGER = Bukkit.getLogger();
    /**
     * The JavaPlugin instance
     */
    private final JavaPlugin plugin;
    /**
     * The config map which contains all the configs
     */
    private final Map<Class<? extends Config>, Map<String, Config>> configMap = new HashMap<>();

    private static ConfigManager instance;

    /**
     * Creates a new instance of the LangManager
     *
     * @param plugin the plugin to create the instance for
     * @return the created instance
     */
    public static ConfigManager createInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new ConfigManager(plugin);
        }
        return instance;
    }

    /**
     * Gets the instance of the LangManager use {@link ConfigManager#createInstance(JavaPlugin)} before to initialize the instance
     *
     * @return the instance of the LangManager or null if not initialized correctly
     */
    public static ConfigManager instance() {
        if (instance == null) {
            throw new IllegalStateException("ConfigManager instance has not been initialized!");
        }
        return instance;
    }

    private ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }


    /**
     * Adds a config to the manager
     *
     * @param name   the name to reference the config by (can overwrite existing configs if the name is the same)
     * @param config the config to add
     */
    public synchronized void add(@NotNull String name, @NotNull Config config) {
        configMap.computeIfAbsent(config.getClass(), k -> new HashMap<>());
        configMap.get(config.getClass()).put(name, config);
        config.silentLoad();
    }

    /**
     * Loads all configs
     */
    public synchronized void load() {
        configMap.values().forEach(configs -> configs.values().forEach(Config::load));
    }

    /**
     * Loads all configs silently
     */
    public synchronized void silentLoad() {
        configMap.values().forEach(configs -> configs.values().forEach(Config::silentLoad));
    }

    /**
     * Saves all configs
     */
    public synchronized void save() {
        configMap.values().forEach(configs -> configs.values().forEach(Config::save));
    }

    /**
     * Saves all configs silently
     */
    public synchronized void silentSave() {
        configMap.values().forEach(configs -> configs.values().forEach(Config::silentSave));
    }

    /**
     * Gets a config by its file name not the key set by the {@link #add(String, Config)}
     * <br>
     * Can cause unexpected results if there are multiple configs with the same name
     *
     * @param name the name of the config file
     * @return the config or null
     */
    public synchronized <T> Config getConfigByName(String name) {
        for (var configEntry : configMap.values()) {
            for (var entry : configEntry.entrySet()) {
                if (entry.getValue().name().equalsIgnoreCase(name)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Gets a Config by its name and class automatically casts it to the class
     *
     * @param name the name of the config
     * @return the config or null
     */
    @SuppressWarnings("unchecked")
    public synchronized <T extends Config> T getConfig(String name, Class<T> clazz) {
        return (T) configMap.get(clazz).get(name);
    }

    /**
     * Gets a Config by its name
     *
     * @param name the name of the config
     * @return the config or an empty optional if not found
     */
    public synchronized Optional<Config> getConfig(String name) {
        for (Map<String, Config> configs : configMap.values()) {
            if (configs.containsKey(name)) {
                return Optional.of(configs.get(name));
            }
        }
        return Optional.empty();
    }

    /**
     * Should be called on shutdown to save all configs back to file
     */
    public void onShutdown() {
        if (!configMap.isEmpty()) {
            silentSave();
            LOGGER.log(Level.SEVERE, "Saved " + configMap.size() + " configs!");
        }
    }

    /**
     * Adds all config yml files from a given path (the name they are stored under is the file name)
     *
     * @param path the path to add the configs from
     * @return a map of the configs added
     */
    public synchronized Map<String, Config> addAllConfigsFromPath(Path path) {
        File[] files = Path.of(plugin.getDataFolder().getPath(), path.toString()).toFile().listFiles();
        Map<String, Config> tempConfigs = new HashMap<>();
        if (files == null) {
            return null;
        }
        for (File file : files) {
            if (!file.isFile()) {
                continue;
            }
            if (!file.getName().endsWith(".yml")) {
                continue;
            }
            Config config = new ConfigYML(plugin, file.toPath());
            add(config.name(), config);
            tempConfigs.put(file.getName(), config);
        }

        return tempConfigs;
    }

    /**
     * Gets all configs stored in the manager
     * @return a collection of all configs
     */
    public Collection<Config> getConfigs() {
        return configMap.values().stream().flatMap(map -> map.values().stream()).toList();
    }

    /**
     * Gets all configs stored in the manager
     * @return a collection of all configs
     */
    public Map<Class<? extends Config>, Map<String, Config>> getConfigMap() {
        return configMap;
    }
}