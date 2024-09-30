package com.wonkglorg.utilitylib.config.types;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Wonkglorg
 */
@SuppressWarnings({"unused", "ResultOfMethodCallIgnored"})
public class ConfigYML extends YamlConfiguration implements Config {

    //Add version control. Keep version in yml and add all new values which do not exist yet if current version is higher than the one already existing
    protected final JavaPlugin PLUGIN;
    protected final String NAME;
    protected final Path SOURCE_PATH;
    protected final Path DESTINATION_PATH;
    protected final File FILE;
    protected final Logger LOGGER = Bukkit.getLogger();

    /**
     * Creates a new file at the specified location or copies an existing one from the resource folder based on the sourcePath,
     * if nothing could be found in the sourcePath it creates a new one. DestinationPath will automatically point to the plugin data folder.
     *
     * @param plugin          plugin instance
     * @param sourcePath      path inside the resources folder of your plugin
     * @param destinationPath path to copy this file to
     */
    public ConfigYML(@NotNull JavaPlugin plugin, @NotNull Path sourcePath, @NotNull Path destinationPath) {
        this.PLUGIN = plugin;
        this.NAME = destinationPath.getFileName().toString();
        this.SOURCE_PATH = sourcePath;
        this.DESTINATION_PATH = destinationPath.startsWith(plugin.getDataFolder().toString()) ? destinationPath : Path.of(plugin.getDataFolder().toString(), destinationPath.toString());
        FILE = new File(this.DESTINATION_PATH.toString());
    }


    /**
     * Creates a new file at the specified location or copies an existing one from the resource folder based on the name,
     * if nothing could be found in the resource folder it creates a new one. name will automatically point to the base of the plugin data folder
     *
     * @param plugin plugin instance
     * @param name   Both the name for destination and source
     */
    public ConfigYML(@NotNull JavaPlugin plugin, @NotNull String name) {
        this(plugin, Path.of(name), Path.of(plugin.getDataFolder().getPath(), name));
    }

    /**
     * Creates a new file at the specified location or copies an existing one from the resource folder based on the path,
     * if nothing could be found in the resource folder it creates a new one. path will automatically point to the base of the plugin data folder
     *
     * @param plugin plugin instance
     * @param path   both the source and destination path
     */
    public ConfigYML(@NotNull JavaPlugin plugin, @NotNull Path path) {
        this(plugin, path, Path.of(plugin.getDataFolder().getPath(), path.toString()));
    }

    /**
     * Gets a section of the config at the set path.
     *
     * @param path path inside yml config.
     * @param deep deep search to get children of children
     * @return {@link Set} of results.
     */
    public Set<String> getSection(String path, boolean deep) {
        ConfigurationSection section = getConfigurationSection(path);
        if (section != null) {
            return section.getKeys(deep);
        }
        return new HashSet<>();
    }

    /**
     * gets a section of the config at the set path with a value to automatically cast to
     *
     * @param path path inside yml config.
     * @param deep deep search to get children of children
     * @param <T>  type of the map
     * @return {@link Map} of results.
     */
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getValues(String path, boolean deep) {
        if (path == null || path.isBlank()) {
            return (Map<String, T>) getValues(deep);
        }
        ConfigurationSection section = getConfigurationSection(path);
        if (section != null) {
            return (Map<String, T>) section.getValues(deep);
        }
        return Map.of();
    }

    public @Nullable String getParentPath(@NotNull String path) {
        ConfigurationSection currentSection = getConfigurationSection(path);
        if (currentSection == null) {
            return null;
        }

        ConfigurationSection configurationSection = currentSection.getParent();
        if (configurationSection == null) {
            return null;
        }

        return configurationSection.getCurrentPath();
    }

    public void setItemStack(String path, ItemStack itemStack) {
        set(path, itemStack.serialize());
    }

    public void setLocation(String path, Location location) {
        set(path, location.serialize());
    }

    public void updateConfig() {

        FileConfiguration existing = YamlConfiguration.loadConfiguration(FILE);

        FileConfiguration newConfig = YamlConfiguration.loadConfiguration(SOURCE_PATH.toFile());

        for (Entry<String, Object> entry : newConfig.getValues(true).entrySet()) {
            if (!existing.contains(entry.getKey())) {
                existing.set(entry.getKey(), entry.getValue());
            }
        }
    }

    public void load() {
        checkFile();
        try {
            load(FILE);
            LOGGER.log(Level.INFO, "Loaded data from " + NAME + "!");
        } catch (InvalidConfigurationException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            LOGGER.log(Level.WARNING, "Error loading data from " + NAME + "!");
        }
    }

    public void silentLoad() {
        checkFile();
        try {
            load(FILE);
        } catch (InvalidConfigurationException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            LOGGER.log(Level.WARNING, "Error loading data from " + NAME + "!");
        }
    }

    public void save() {
        checkFile();
        try {

            save(FILE);
            LOGGER.log(Level.INFO, "Saved data to " + NAME + "!");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            LOGGER.log(Level.WARNING, "Error saving data to " + NAME + "!");
        }
    }

    public void silentSave() {
        checkFile();
        try {
            save(FILE);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            LOGGER.log(Level.WARNING, "Error saving data to " + NAME + "!");
        }
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String path() {
        return DESTINATION_PATH.toString();
    }

    /**
     * Checks if file exists in path, else create the file and all parent directories needed.
     */
    protected void checkFile() {
        if (!FILE.exists()) {
            FILE.getParentFile().mkdirs();
            InputStream inputStream = PLUGIN.getResource(SOURCE_PATH.toString().replaceAll("\\\\", "/"));
            if (inputStream != null) {
                try {
                    Files.copy(inputStream, DESTINATION_PATH);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error Copying data from " + SOURCE_PATH + " to destination " + DESTINATION_PATH);
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            } else {
                FILE.getParentFile().mkdirs();
                try {
                    FILE.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public String toString() {
        return String.format("ConfigYML[path=%s,name=%s]", DESTINATION_PATH.toString(), NAME);
    }

}