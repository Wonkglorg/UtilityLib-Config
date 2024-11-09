package com.wonkglorg.utilitylib.config;

import com.wonkglorg.utilitylib.config.types.Config;
import com.wonkglorg.utilitylib.config.types.LangConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Configmanager to handle accessing configs.
 * <br>
 * <br>
 * BEFORE USING THIS CLASS MAKE SURE TO CALL {@link ConfigManager#createInstance(JavaPlugin)} TO INITIALIZE THE INSTANCE  (only needs to be done once during the whole runtime)
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
     * Maps locales with the same base language name to its language name (to easier assign all relevant names to this from file alone)
     */
    private static final Map<String, Set<Locale>> shortNameToLocaleMapper = new ConcurrentHashMap<>();

    static {
        for (Locale locale : Locale.getAvailableLocales()) {
            shortNameToLocaleMapper.computeIfAbsent(locale.getLanguage(), k -> new HashSet<>()).add(locale);
        }
    }

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
     * Sets the default language to use when no user language could be determined
     *
     * @param defaultLang the default language locale
     */
    public synchronized void setDefaultLang(Locale defaultLang) {
        this.defaultLang = defaultLang;
    }

    /**
     * Adds a language to the lang manager this is for single locales specifically to get a whole subsection of locales registered use the prefered method {@link #addLanguage(LangConfig, String, String...)} where each string is its {@link Locale#getLanguage()} definition
     *
     * @param locale         1 or more locale this config should apply to
     * @param languageConfig the language config
     */
    public synchronized void addLanguage(LangConfig languageConfig, Locale locale, Locale... extraLocale) {
        langMap.putIfAbsent(locale, languageConfig);
        for (Locale loc : extraLocale) {
            langMap.putIfAbsent(loc, languageConfig);
        }
        languageConfig.silentLoad();
    }

    /**
     * Adds a language to the lang manager this is a more generic version of {@link #addLanguage(LangConfig, Locale, Locale...)},
     * since multiple locales share the same language code this method allows to add all common locales based on its language code(example:
     * <pre>
     *     {@code "en" -> en_US, en_GB, en_CA, etc...}
     * @param languageConfig the language config
     * @param langName the name of the language derived from {@link Locale#getLanguage()}
     * @param extraLangNames extra names to add to the language
     */
    public synchronized void addLanguage(LangConfig languageConfig, String langName, String... extraLangNames) {
        Set<Locale> locales = shortNameToLocaleMapper.get(langName);
        if (locales == null) {
            LOGGER.log(Level.WARNING, "No locale found for file: " + langName);
            return;
        }
        for (Locale locale : locales) {
            addLanguage(languageConfig, locale);
        }
        for (String extraLangName : extraLangNames) {
            locales = shortNameToLocaleMapper.get(extraLangName);
            if (locales == null) {
                LOGGER.log(Level.WARNING, "No locale found for file: " + extraLangName);
                continue;
            }
            for (Locale locale : locales) {
                addLanguage(languageConfig, locale);
            }
        }
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
     * Adds all language files from a given path, the path should be relative to the plugin data folder, the language files should be named after the language they represent as per {@link Locale#getLanguage()} standard naming conventions (this does not copy them from the resources folder should be used to let the plugin user add more langs on their own without code changes)
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
            //todo should specific langs be implemted? so if a file is set to en_US it only applies to en_US instead of all of en?

            Set<Locale> locales = shortNameToLocaleMapper.get(file.getName().replace(".yml", ""));
            if (locales == null) {
                LOGGER.log(Level.WARNING, "No locale found for file: " + file.getName());
                continue;
            }

            for (Locale locale : locales) {
                LangConfig langConfig = new LangConfig(plugin, path.resolve(file.getName()).toString());
                addLanguage(langConfig, locale);
            }

        }
    }


    /**
     * Gets a value from the default language file with replacements applied
     *
     * @param key the key to get ny
     * @return the returned result or the key if no result was found
     */
    @Contract(pure = true, value = "null -> null")
    public String getValue(String key) {
        return getValue((Locale) null, key, key);
    }


    /**
     * Gets a value from the default language file with replacements applied
     *
     * @param locale the locale to get the value from
     * @param key    the key to get by
     * @return the returned result or the value if no result was found
     */
    @Contract(pure = true, value = "_, null -> null")
    public String getValue(final Locale locale, final String key) {
        return getValue(locale, key, key);
    }


    /**
     * Gets a value from the default language file with replacements applied
     *
     * @param player the player to determine the locale for
     * @param key    the key to get by
     * @return the returned result or the value if no result was found
     */
    @Contract(pure = true, value = "_, null -> null")
    public String getValue(final Player player, final String key) {
        return getValue(Locale.forLanguageTag(player.getLocale()), key, key);
    }

    /**
     * Gets a value from the default language file with replacements applied
     *
     * @param player       the player to determine the locale to get the value from
     * @param key          the key to get by
     * @param defaultValue the default value to return if no value was found
     * @return the returned result or the value if no result was found
     */
    @Contract(pure = true, value = "_,null,null -> null; _,_,!null -> !null")
    public String getValue(final Player player, final String key, final String defaultValue) {
        return getValue(Locale.forLanguageTag(player.getLocale()), key, defaultValue);
    }

    /**
     * Gets a value from the default language file with replacements applied
     *
     * @param locale       the locale to get the value from
     * @param key          the key to get by
     * @param defaultValue the default value to return if no value was found
     * @return the returned result or the value if no result was found
     */
    @Contract(pure = true, value = "_,null,null -> null; _,_,!null -> !null")
    public String getValue(final Locale locale, final String key, final String defaultValue) {
        LangConfig config;

        var configOptional = getAnyValidLangConfig(locale);
        if (configOptional.isPresent()) {
            config = configOptional.get();
        } else {
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
     * Gets any valid language config to use (first checks if the locale is present, then the default locale, then any locale)
     *
     * @param locale the locale to get the language config for
     * @return the language config or empty if none could be found
     */
    private Optional<LangConfig> getAnyValidLangConfig(final Locale locale) {
        if (langMap.isEmpty()) {
            return Optional.empty();
        }

        if (locale != null && langMap.containsKey(locale)) {
            return Optional.of(langMap.get(locale));
        }

        if (langMap.containsKey(defaultLang)) {
            return Optional.of(langMap.get(defaultLang));
        }
        return Optional.of(langMap.values().iterator().next());
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
    @Contract(pure = true, value = "null -> null")
    public synchronized LangConfig getLangByFileName(final String name) {
        for (LangConfig config : langMap.values()) {
            if (config.name().equalsIgnoreCase(name)) {
                return config;
            }
        }
        return null;
    }

}