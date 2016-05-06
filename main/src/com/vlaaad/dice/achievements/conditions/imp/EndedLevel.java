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

import com.vlaaad.common.util.filters.Filter;
import com.vlaaad.dice.achievements.conditions.AchievementCondition;
import com.vlaaad.dice.achievements.conditions.util.ConditionUtil;
import com.vlaaad.dice.achievements.events.imp.EndLevelEvent;

import java.util.Map;

/**
 * Created 18.05.14 by vlaaad
 */
public class EndedLevel extends AchievementCondition<EndLevelEvent> {

    private Filter<EndLevelEvent> filter;

    public EndedLevel() {
        super(EndLevelEvent.class);
    }


    @Override public void setup(Object params) {
        Map map = (Map) params;
        filter = ConditionUtil.createLevelFilter(map);
    }

    @Override protected boolean satisfied(EndLevelEvent event) {
        return filter.accept(event);
    }
}
