package com.wonkglorg.utilitylib.config;

import com.wonkglorg.utilitylib.config.types.Config;
import com.wonkglorg.utilitylib.config.types.LangConfig;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Configmanager to handle accessing configs.
 * <br>
 * <br>
 * BEFORE USING THIS CLASS MAKE SURE TO CALL {@link ConfigManager#createInstance(JavaPlugin)} TO INITIALIZE THE INSTANCE
 *
 * @author Wonkglorg
 */
@SuppressWarnings("unused")
public final class LangManager {
    /**
     * The Logger instance
     */
    private final Logger LOGGER = Bukkit.getLogger();
    /**
     * The lang map which contains all the language configs
     */
    private final Map<Locale, LangConfig> langMap = new ConcurrentHashMap<>();
    /**
     * The replacer map which contains all the values to be replaced when called
     */
    private final Map<String, String> replacerMap = new ConcurrentHashMap<>();
    /**
     * The default language
     */
    private Locale defaultLang = Locale.ENGLISH;
    /**
     * The JavaPlugin instance
     */
    private final JavaPlugin plugin;

    private static LangManager instance;

    /**
     * Creates a new instance of the LangManager
     *
     * @param plugin the plugin to create the instance for
     * @return the created instance
     */
    public static LangManager createInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new LangManager(plugin);
        }
        return instance;
    }

    /**
     * Gets the instance of the LangManager use {@link LangManager#createInstance(JavaPlugin)} before to initialize the instance
     *
     * @return the instance of the LangManager or null if not initialized correctly
     */
    public static LangManager instance() {
        if (instance == null) {
            throw new IllegalStateException("LangManager instance has not been initialized!");
        }
        return instance;
    }

    private LangManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Adds a value to be replaced in the lang file whenever the {@link #getValue(String)} method is called
     *
     * @param replace the value to be replaced
     * @param with    the value to replace the original value with
     */
    public void replace(String replace, String with) {
        replacerMap.put(replace, with);
    }

    /**
     * Sets the default language and the default config
     *
     * @param defaultLang   the default language
     * @param defaultConfig the default config
     */
    public synchronized void setDefaultLang(Locale defaultLang, LangConfig defaultConfig) {
        langMap.put(defaultLang, defaultConfig);
        this.defaultLang = defaultLang;
        defaultConfig.silentLoad();
    }

    /**
     * Adds a language to the lang manager
     *
     * @param locale         the locale of the language
     * @param languageConfig the language config
     */
    public synchronized void addLanguage(Locale locale, LangConfig languageConfig) {
        if (langMap.containsKey(locale)) {
            return;
        }
        langMap.putIfAbsent(locale, languageConfig);
        languageConfig.silentLoad();
    }

    /**
     * Saves all the language files
     */
    public synchronized void save() {
        langMap.values().forEach(Config::save);
    }

    public synchronized void silentSave() {
        langMap.values().forEach(Config::silentSave);
    }

    public synchronized void load() {
        langMap.values().forEach(Config::silentLoad);

    }

    public synchronized void silentLoad() {
        langMap.values().forEach(Config::silentLoad);

        if (defaultLang == null) {
            LOGGER.log(Level.WARNING, "No default language selected!");
        }
    }

    public synchronized Config getDefaultLang() {
        try {
            return langMap.get(defaultLang);
        } catch (Exception e) {
            return null;
        }
    }

    public synchronized void addAllLangFilesFromPath(String... paths) {
        if (paths.length == 0) {
            return;
        }
        String first = paths[0];
        String[] more = Arrays.copyOfRange(paths, 1, paths.length);
        Path path = Path.of(first, more);
        addAllLangFilesFromPath(path);
    }


    /**
     * Adds all language files from a given path, the path should be relative to the plugin data folder, the language files should be named after the language they represent as per {@link Locale#getLanguage()} standard naming conventions
     */
    public synchronized void addAllLangFilesFromPath(Path path) {
        File[] files = Path.of(plugin.getDataFolder().getPath(), path.toString()).toFile().listFiles();
        if (files == null) {
            LOGGER.log(Level.WARNING, "No available language files loaded");
            return;
        }
        for (File file : files) {
            if (!file.isFile()) {
                continue;
            }
            if (!file.getName().endsWith(".yml")) {
                continue;
            }
            for (Locale locale : Locale.getAvailableLocales()) {
                if (locale.getLanguage().equalsIgnoreCase(file.getName().replace(".yml", ""))) {
                    LOGGER.log(Level.INFO, file.getName() + " has been loaded!");
                    LangConfig config = new LangConfig(plugin, file.toPath());
                    addLanguage(locale, config);
                }
            }
        }
    }


    /**
     * Gets a value from the default language file with replacements applied
     *
     * @param key the key to get ny
     * @return the returned result or the key if no result was found
     */

    public String getValue(String key) {
        return getValue(null, key, key);
    }

    /**
     * Gets a value from the default language file with replacements applied
     *
     * @param player the player to get the locale from
     * @param key    the key to get by
     * @return the returned result or the value if no result was found
     */
    //public String getValue(Player player, String key) {
    //    return getValue(, player.getLocale(), key, key);
    //}

    /**
     * Gets a value from the default language file with replacements applied
     *
     * @param locale the locale to get the value from
     * @param key    the key to get by
     * @return the returned result or the value if no result was found
     */
    public String getValue(@NotNull final Locale locale, @NotNull final String key) {
        return getValue(locale, key, key);
    }

    /**
     * Gets a value from the default language file with replacements applied
     *
     * @param locale       the locale to get the value from
     * @param key          the key to get by
     * @param defaultValue the default value to return if no value was found
     * @return the returned result or the value if no result was found
     */
    public String getValue(final Locale locale, @NotNull final String key, @NotNull final String defaultValue) {
        LangConfig config;
        if (locale == null) {
            config = langMap.get(defaultLang);
        } else {
            config = langMap.containsKey(locale) ? langMap.get(locale) : langMap.get(defaultLang);
        }

        if (config == null) {
            Bukkit.getLogger().log(Level.INFO, "No lang file could be loaded for request: " + key + " using default value!");
            return defaultValue;
        }

        String editString = config.getString(key);
        if (editString == null) {
            return defaultValue;
        }

        for (var mapValue : replacerMap.entrySet()) {
            editString = editString.replace(mapValue.getKey(), mapValue.getValue());
        }

        if (config.isUpdateRequest()) {
            config.updateReplacerMap();

        }

        for (var mapValue : config.getReplacerMap().entrySet()) {
            editString = editString.replace(mapValue.getKey(), mapValue.getValue());
        }

        return editString;

    }

    /**
     * Gets all stored languages
     *
     * @return the map of all languages
     */
    public synchronized Map<Locale, LangConfig> getAllLangs() {
        return langMap;
    }

    /**
     * Gets a language config by the locale
     *
     * @param name the name of the language file
     * @return the language config or null if not found
     */
    public synchronized LangConfig getLangByFileName(String name) {
        for (LangConfig config : langMap.values()) {
            if (config.name().equalsIgnoreCase(name)) {
                return config;
            }
        }
        return null;
    }

}