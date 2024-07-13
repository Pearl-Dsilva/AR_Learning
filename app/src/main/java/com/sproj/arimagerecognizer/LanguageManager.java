package com.sproj.arimagerecognizer;

import android.content.SharedPreferences;

import com.google.mlkit.nl.translate.TranslateLanguage;

import java.util.Map;

public class LanguageManager {
    SharedPreferences sharedPreferences;

    static final Map<String, String> availableLanguages = Map.of(
            "CHINESE", TranslateLanguage.CHINESE,
            "SPANISH", TranslateLanguage.SPANISH,
            "FRENCH", TranslateLanguage.FRENCH,
            "KANNADA", TranslateLanguage.KANNADA);


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





