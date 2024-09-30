package com.wonkglorg.utilitylib.config.langs;

public enum Lang {
    ENGLISH("en-us"),
    SPANISH("es-es"),
    FRENCH("fr-fr"),
    GERMAN("de-de"),
    ITALIAN("it-it"),
    DUTCH("nl-nl"),
    PORTUGUESE("pt-pt"),
    RUSSIAN("ru-ru"),
    JAPANESE("ja-jp"),
    CHINESE("zh-cn"),
    KOREAN("ko-kr");

    private final String locale;

    Lang(String locale) {
        this.locale = locale;
    }

    public String getLocale() {
        return locale;
    }
}
