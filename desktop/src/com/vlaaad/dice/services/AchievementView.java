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

package com.vlaaad.dice.services;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.achievements.Achievement;
import com.vlaaad.dice.game.world.view.Tile;
import com.vlaaad.dice.ui.scene2d.LocLabel;

/**
 * Created 16.05.14 by vlaaad
 */
public class AchievementView extends Table {
    public AchievementView(Achievement achievement) {
        super(Config.skin);
        defaults().pad(2);
        setTouchable(Touchable.disabled);
        setBackground("particle-white-pixel");
        add(new LocLabel("achievement-" + achievement.name, Color.BLACK)).expand().padTop(-1);
        setSize(153, 28);
    }
}
