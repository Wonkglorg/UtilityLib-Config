package com.wonkglorg.utilitylib.config.types;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class LangConfig extends ConfigYML {

    /**
     * Path to the placeholder definitions in the lang file, all keys defined under this path will be added to the automatic replacer map
     * (default: "placeholders")
     *
     * <p>Example:
     * <pre>
     *     placeholders:
     *          mod-name: "My Mod Name"
     *          mod-version: "1.0.0"
     * </pre>
     * This structure defines that all occurrences of %mod-name% will be replaced by "My Mod Name" and %mod-version% by "1.0.0"
     */
    private String placeholderPath = "placeholders";
    private char placeholderChar = '%';
    /**
     * Update request used when the replacer map needs to be updated
     */
    private boolean updateRequest = false;
    /**
     * Map of placeholders and their values to replace them by
     */
    private final Map<String, String> replacerMap = new ConcurrentHashMap<>();

    /**
     * Constructor for the LangConfig class
     *
     * @param plugin          the plugin the config is for
     * @param sourcePath      the path to the source file
     * @param destinationPath the path to the destination file
     */
    public LangConfig(@NotNull JavaPlugin plugin, @NotNull Path sourcePath, @NotNull Path destinationPath) {
        super(plugin, sourcePath, destinationPath);
    }

    /**
     * Constructor for the LangConfig class
     *
     * @param plugin the plugin the config is for
     * @param name   the name of the file used to determine the path to copy the file from/to
     */
    public LangConfig(@NotNull JavaPlugin plugin, @NotNull String name) {
        super(plugin, name);
    }

    /**
     * Constructor for the LangConfig class
     *
     * @param plugin the plugin the config is for
     * @param path   the path to the file both for input and output (relative to the plugin data folder)
     */
    public LangConfig(@NotNull JavaPlugin plugin, @NotNull Path path) {
        super(plugin, path);
    }

    @Override
    public void load() {
        setUpdateRequest(true);
        checkFile();
        try {
            load(FILE);
            LOGGER.log(Level.INFO, "Loaded data from " + NAME + "!");
        } catch (InvalidConfigurationException | IOException e) {
            LOGGER.log(Level.WARNING, "Error loading data from " + NAME + "!");
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void silentLoad() {
        setUpdateRequest(true);
        checkFile();
        try {
            load(FILE);
        } catch (InvalidConfigurationException | IOException e) {
            LOGGER.log(Level.WARNING, "Error loading data from " + NAME + "!");
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void updateReplacerMap() {
        if (this.isSet(this.getPlaceholderPath())) {
            String path = this.getPlaceholderPath();
            for (Map.Entry<String, Object> entry : getEntries(path).entrySet()) {
                String placeholderValue = entry.getValue().toString();
                String searchKey = placeholderChar + entry.getKey() + placeholderChar;
                replacerMap.put(searchKey, placeholderValue);
            }
        }

        setUpdateRequest(false);
    }

    /**
     * @return the replacer map of all keys to be replaced and their values
     */
    public Map<String, String> getReplacerMap() {
        if (isUpdateRequest()) updateReplacerMap();
        return replacerMap;
    }

    /**
     * @param placeholderString the path to the placeholder definitions in the lang file (default: "placeholders")
     */
    public void setPlaceholderPath(String placeholderString) {
        this.placeholderPath = placeholderString;
    }

    public String getPlaceholderPath() {
        return placeholderPath;
    }

    /**
     * @return true if an update of the replacer map is requested but not yet performed
     */
    public boolean isUpdateRequest() {
        return updateRequest;
    }

    /**
     * @param updateRequest when set to true updates the replacer map when next requested
     */
    public void setUpdateRequest(boolean updateRequest) {
        this.updateRequest = updateRequest;
    }

    /**
     * @return the character used to denote placeholders
     */
    public char getPlaceholderChar() {
        return placeholderChar;
    }

    /**
     * @param placeholderChar the character used to denote placeholders
     */
    public void setPlaceholderChar(char placeholderChar) {
        this.placeholderChar = placeholderChar;
    }
}