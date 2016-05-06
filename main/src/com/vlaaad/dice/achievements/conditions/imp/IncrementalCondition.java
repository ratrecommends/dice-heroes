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

import com.vlaaad.dice.achievements.conditions.AchievementCondition;
import com.vlaaad.dice.achievements.events.Event;
import com.vlaaad.dice.achievements.events.EventType;

public class IncrementalCondition<T extends Event> extends AchievementCondition<T> {

    private int current;
    private int total;

    public IncrementalCondition(EventType<T> eventType) {
        super(eventType);
    }

    public IncrementalCondition(Class<T> eventClass) {
        super(eventClass);
    }

    public IncrementalCondition() {
    }

    @Override public void setup(Object params) {
        total = ((Number) params).intValue();
    }

    @Override protected boolean satisfied(T t) {
        current++;
        return current >= total;
    }

    public int getCurrent() { return current; }

    public int getTotal() { return total; }

    @Override public void load(Object o) {
        current = ((Number) o).intValue();
    }

    @Override public Object save() {
        return current;
    }
}
