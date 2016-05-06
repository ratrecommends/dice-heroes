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

import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.achievements.Achievement;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.ui.scene2d.LocLabel;

/**
 * Created 16.05.14 by vlaaad
 */
public class AchievementsWindow extends GameWindow<UserData> {

    @Override protected void initialize() {
        Table content = new Table(Config.skin);
        content.setBackground("ui-creature-info-background");
        content.add(new LocLabel("ui-achievements")).row();
        Array<Achievement> sorted = new Array<Achievement>();
        for (Achievement achievement : Config.achievements) {
            sorted.add(achievement);
        }
        sorted.sort(Achievement.COMPARATOR);
        Table list = new Table();
        for (Achievement achievement : sorted) {
            list.add(new AchievementDescriptionView(achievement)).row();
        }

        content.add(new ScrollPane(list)).height(100);
        table.add(content);
    }

    @Override protected void doShow(UserData userData) {
    }
}
