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

import com.vlaaad.common.util.Logger;
import com.vlaaad.dice.achievements.conditions.AchievementCondition;
import com.vlaaad.dice.achievements.events.Event;

/**
 * Created 18.05.14 by vlaaad
 */
public class AllOfCondition extends AnyOf {
    @Override protected boolean satisfied(Event event) {
//        Logger.debug("check all of", new RuntimeException());
        for (AchievementCondition child : conditions) {
//            Logger.debug("check "+ child);
            if (!child.isSatisfied(event)) {
//                Logger.debug("not satisfied!");
                return false;
            }
        }
        return true;
    }
}
