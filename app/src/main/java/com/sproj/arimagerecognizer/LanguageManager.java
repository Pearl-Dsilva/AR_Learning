package com.sproj.arimagerecognizer;

import android.content.SharedPreferences;

import com.google.mlkit.nl.translate.TranslateLanguage;

import java.util.Map;
import java.util.AbstractMap.SimpleEntry;

public class LanguageManager {
    SharedPreferences sharedPreferences;

    public static final Map<String, String> availableLanguages = Map.ofEntries(
            new SimpleEntry<>("CHINESE", TranslateLanguage.CHINESE),
            new SimpleEntry<>("SPANISH", TranslateLanguage.SPANISH),
            new SimpleEntry<>("FRENCH", TranslateLanguage.FRENCH),
            new SimpleEntry<>("RUSSIAN", TranslateLanguage.RUSSIAN),
            new SimpleEntry<>("GERMAN", TranslateLanguage.GERMAN),
            new SimpleEntry<>("KANNADA", TranslateLanguage.KANNADA),
            new SimpleEntry<>("Gujarati", TranslateLanguage.GUJARATI),
            new SimpleEntry<>("Bengali", TranslateLanguage.BENGALI),
            new SimpleEntry<>("Marathi", TranslateLanguage.MARATHI),
            new SimpleEntry<>("Japanese", TranslateLanguage.JAPANESE),
            new SimpleEntry<>("ITALIAN", TranslateLanguage.ITALIAN),
            new SimpleEntry<>("HINDI", TranslateLanguage.HINDI),
            new SimpleEntry<>("Telugu", TranslateLanguage.TELUGU),
            new SimpleEntry<>("Urdu", TranslateLanguage.URDU),
            new SimpleEntry<>("Arabic", TranslateLanguage.ARABIC),
            new SimpleEntry<>("Korean", TranslateLanguage.KOREAN),
            new SimpleEntry<>("German", TranslateLanguage.GERMAN)
    );


    public LanguageManager(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void modelDownloaded(String modelName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(modelName, true);
        editor.apply();
    }

    public void languageSelected(int language) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("language", language);
        editor.apply();
    }

    public int getLanguage() {
        return sharedPreferences.getInt("language", 0);
    }

    public void modelDeleted(String modelName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(modelName);
        editor.apply();
    }

    public boolean isModelDownloaded(String modelName) {
        return sharedPreferences.getBoolean(modelName, false);
    }

    public Map<String, String> getAvailableLanguage() {
        return availableLanguages;
    }

    public boolean isSetupCompleted() {
        return sharedPreferences.getBoolean("setup", false);
    }

    public void setupCompleted() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("setup", true);
        editor.apply();
    }
}





