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

package com.vlaaad.dice.ui.scene2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.thesaurus.Localizable;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;

/**
 * Created 05.11.13 by vlaaad
 */
public class LocLabel extends Label implements Localizable {
    private String key;
    private ObjectMap<String, String> params;

    public LocLabel(String key) {
        this(key, Thesaurus.EMPTY);
        validate();
    }

    public LocLabel(String key, ObjectMap<String, String> params) {
        super(key, Config.skin);
        this.key = key;
        this.params = params;
    }

    public LocLabel(String key, String colorName) {
        super(key, Config.skin, "default", colorName);
        this.key = key;
        this.params = Thesaurus.EMPTY;
    }

    public LocLabel(String key, Color color) {
        super(key, Config.skin, "default", color);
        this.key = key;
        this.params = Thesaurus.EMPTY;
    }

    public LocLabel(String key, ObjectMap<String, String> params, Color color) {
        super(key, Config.skin, "default", color);
        this.key = key;
        this.params = params;
    }

    public LocLabel(String key, ObjectMap<String, String> params, String styleName) {
        super(key, Config.skin, styleName);
        this.key = key;
        this.params = params;
    }

    public LocLabel(Thesaurus.LocalizationData localizationData) {
        this(localizationData.key, localizationData.params);
    }

    public LocLabel(Thesaurus.LocalizationData data, Color color) {
        this(data.key, data.params, color);
    }

    @Override public void invalidate() {
        super.invalidate();
    }

    @Override public void layout() {
        setText(Config.thesaurus.localize(key, params));
        super.layout();
    }

    @Override protected void setStage(Stage stage) {
        if (stage == null && getStage() != null) {
            //unregister
            Config.thesaurus.unregister(this);
        } else if (stage != null && getStage() == null) {
            //register
            Config.thesaurus.register(this, key, params);
        }
        super.setStage(stage);
    }

    public void setKey(String key) {
        if (this.key.equals(key))
            return;
        this.key = key;
        if (getStage() != null) {
            Config.thesaurus.register(this, key, params);
        }
    }

    public void setParams(ObjectMap<String, String> params) {
        if (this.params.equals(params))
            return;
        this.params = params;
        if (getStage() != null) {
            Config.thesaurus.register(this, key, params);
        }
    }

    @Override public void localize(String localizedString) {
        setText(localizedString);
    }

    public ObjectMap<String, String> getParams() {
        return params;
    }
}
