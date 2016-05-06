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

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.thesaurus.Localizable;

/**
 * Created 11.12.13 by vlaaad
 */
public class LocImage extends Image implements Localizable {
    private final String key;

    public LocImage(String key) {
        super(Config.skin, Config.thesaurus.localize(key));
        this.key = key;
    }

    @Override protected void setStage(Stage stage) {
        if (stage == null && getStage() != null) {
            //unregister
            Config.thesaurus.unregister(this);
        } else if (stage != null && getStage() == null) {
            //register
            Config.thesaurus.register(this, key);
        }
        super.setStage(stage);
    }

    @Override public void localize(String localizedString) {
        setDrawable(Config.skin.getDrawable(localizedString));
    }
}
