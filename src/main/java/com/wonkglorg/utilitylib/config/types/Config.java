package com.wonkglorg.utilitylib.config.types;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Wonkglorg
 */
@SuppressWarnings("unused")
public interface Config {


    @Nullable
    String getString(@NotNull String path);

    int getInt(@NotNull String path);

    /**
     * Returns the double value of a specified path
     *
     * @param path Path to check
     * @return the double value if existing, otherwise 0
     */
    double getDouble(@NotNull String path);

    /**
     * Returns the long value of a specified path
     *
     * @param path Path to check
     * @return the long value if existing, otherwise 0
     */
    long getLong(@NotNull String path);

    /**
     * Returns the boolean value of the specified path
     *
     * @param path Path to check
     * @return true if the value exists and is set to true, false otherwise
     */
    boolean getBoolean(@NotNull String path);

    List<String> getStringList(@NotNull String path);

    List<Integer> getIntegerList(@NotNull String path);

    List<Double> getDoubleList(@NotNull String path);

    List<Character> getCharacterList(@NotNull String path);

    List<Long> getLongList(@NotNull String path);

    List<Boolean> getBooleanList(@NotNull String path);

    List<Map<?, ?>> getMapList(@NotNull String path);

    <T> T getObject(@NotNull String path, @NotNull Class<T> clazz);

    Location getLocation(@NotNull String path);

    ItemStack getItemStack(@NotNull String path);

    Color getColor(@NotNull String path);

    OfflinePlayer getOfflinePlayer(@NotNull String path);

    /**
     * Gets values from a specific section.
     */
    Set<String> getKeys(String path, boolean deep);

    <T> Map<String, T> getEntries(@NotNull String path);

    /**
     * Checks if a path exists within the config
     *
     * @param path Path to check
     * @return true if the path exists, false otherwise
     */
    boolean contains(@NotNull String path);

    /**
     * Sets a data value type as the value at a specific path, if the path does not yet exist create it
     *
     * @param path  Path to put
     * @param value Value to set
     */
    void set(@NotNull String path, Object value);

    /**
     * Loads config from file.
     */
    void load();

    /**
     * Loads config without Log message
     */
    void silentLoad();

    /**
     * Saves config to file.
     */
    void save();

    /**
     * Saves config without log message
     */
    void silentSave();

    /**
     * Returns the name of the config file
     *
     * @return Config file name
     */
    String name();

    /**
     * Returns the path of the config including the name
     *
     * @return Config file path
     */
    String path();
}