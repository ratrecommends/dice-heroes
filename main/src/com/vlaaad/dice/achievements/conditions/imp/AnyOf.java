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
import com.vlaaad.dice.achievements.conditions.AchievementCondition;
import com.vlaaad.dice.achievements.conditions.AchievementConditionFactory;
import com.vlaaad.dice.achievements.events.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created 16.05.14 by vlaaad
 */
public class AnyOf extends AchievementCondition<Event> {

    protected final Array<AchievementCondition> conditions = new Array<AchievementCondition>();

    @SuppressWarnings("unchecked")
    @Override public void setup(Object params) {
        Iterable<Map> sub = (Iterable<Map>) params;
        for (Map map : sub)
            conditions.add(AchievementConditionFactory.create(map));
    }

    @Override protected boolean satisfied(Event event) {
        for (AchievementCondition child : conditions) {
            if (child.isSatisfied(event))
                return true;
        }
        return false;
    }

    @Override public Object save() {
        List<Object> result = new ArrayList<Object>();
        boolean shouldBeSaved = false;
        for (AchievementCondition condition : conditions) {
            Object o = condition.save();
            if (o != null) {
                shouldBeSaved = true;
            }
            result.add(o);
        }
        if (!shouldBeSaved)
            return null;
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override public void load(Object o) {
        List<Object> list = (List<Object>) o;
        int i = 0;
        for (Object object : list) {
            if (object != null) {
                conditions.get(i).load(object);
            }
            i++;
        }
    }

    @Override protected void fillChildren(Array<AchievementCondition> target) {
        target.addAll(conditions);
    }
}
