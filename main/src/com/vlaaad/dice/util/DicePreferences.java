/*
 * Dice heroes is a turn based rpg-strategy game where characters are dice.
 * Copyright (C) 2016 Vladislav Protsenko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.vlaaad.dice.util;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.MathUtils;
import com.vlaaad.common.gdx.App;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.managers.SoundManager;

import java.util.Locale;

/**
 * Created 23.10.13 by vlaaad
 */
public class DicePreferences {
    private final Preferences preferences;
    private final App app;

    private int scale;
    private String language;
    private float volume = 1f;
    private boolean rated;
    private boolean music;
    private boolean servicesPaneShownByDefault;

    public DicePreferences(Preferences preferences, App app) {
        this.preferences = preferences;
        this.app = app;
        volume = preferences.getFloat("volume", 0.5f);
        language = preferences.getString("language", Locale.getDefault().getLanguage());
        scale = preferences.getInteger("scale", (int) ScreenHelper.scaleFor(Gdx.graphics.getWidth(), Gdx.app.getType() == Application.ApplicationType.Desktop));
        rated = preferences.getBoolean("rated");
        music = preferences.getBoolean("music", true);
        servicesPaneShownByDefault = preferences.getBoolean("services-pane", false);
        applyVolume();
        applyMusic();
    }

    public boolean isMusic() {
        return music;
    }

    public void setMusic(boolean music) {
        if (music == this.music)
            return;
        this.music = music;
        applyMusic();
        preferences.putBoolean("music", music);
        preferences.flush();
    }

    private void applyMusic() {
        SoundManager.instance.setUsesMusic(music);
    }

    private void applyScale() {
        app.setScale(scale);
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float v) {
        v = MathUtils.clamp(v, 0, 1);
        if (v == volume)
            return;
        volume = v;
        applyVolume();
        preferences.putFloat("volume", v);
        preferences.flush();
    }

    private void applyVolume() {
        SoundManager.instance.setVolume(volume);
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        if (language == null)
            throw new IllegalArgumentException("language can't be null");
        if (language.equals(this.language))
            return;
        this.language = language;
        preferences.putString("language", language);
        preferences.flush();
        applyLanguage();
    }

    private void applyLanguage() {
        if (Config.thesaurus != null) {
            Config.thesaurus.setLanguage(language);
        }
    }



    public boolean isServicesPaneShownByDefault() { return servicesPaneShownByDefault; }

    public void setServicesPaneShownByDefault(boolean servicesPaneShownByDefault) {
        if (this.servicesPaneShownByDefault == servicesPaneShownByDefault)
            return;
        this.servicesPaneShownByDefault = servicesPaneShownByDefault;
        preferences.putBoolean("services-pane", servicesPaneShownByDefault);
        preferences.flush();
    }

    public boolean isRated() { return rated; }

    public void setRated(boolean rated) {
        if (rated == this.rated)
            return;
        this.rated = rated;
        preferences.putBoolean("rated", rated);
        preferences.flush();
    }

    public int getScale() { return scale; }

    public void setScale(int value) {
        if (value == scale)
            return;
        scale = value;
        applyScale();
        preferences.putInteger("scale", scale);
        preferences.flush();
    }
}
