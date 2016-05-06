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

package com.vlaaad.dice.achievements.conditions.imp;

import com.badlogic.gdx.utils.Array;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.achievements.conditions.AchievementCondition;
import com.vlaaad.dice.achievements.events.Event;
import com.vlaaad.dice.game.config.levels.BaseLevelDescription;
import com.vlaaad.dice.game.config.levels.LevelDescription;

/**
 * Created 18.05.14 by vlaaad
 */
public class LevelsCompleted extends AchievementCondition {

    private final Array<BaseLevelDescription> levels = new Array<BaseLevelDescription>(1);

    @SuppressWarnings("unchecked")
    @Override public void setup(Object params) {
        Iterable<String> levelNames = (Iterable<String>) params;
        for (String levelName : levelNames) {
            levels.add(Config.levels.get(levelName));
        }
    }

    @Override protected boolean satisfied(Event event) {
        for (BaseLevelDescription level : levels) {
            if (!event.userData().isPassed(level))
                return false;
        }
        return true;
    }
}
