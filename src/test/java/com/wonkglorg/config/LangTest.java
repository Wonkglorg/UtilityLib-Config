package com.wonkglorg.config;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.*;

public class LangTest {
    @Test
    public void testLangRecognition() {
        // Test code here


        Map<String, List<Locale>> map = new HashMap<>();
        for (Locale locale : Locale.getAvailableLocales()) {
            map.computeIfAbsent(locale.getLanguage(), k -> new ArrayList<>()).add(locale);
        }

        System.out.println(Locale.forLanguageTag("en-US"));
    }
}
