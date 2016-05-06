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

package com.vlaaad.dice.achievements;

import com.badlogic.gdx.utils.ObjectSet;
import com.vlaaad.dice.achievements.conditions.AchievementCondition;
import com.vlaaad.dice.achievements.events.EventType;

import java.util.Comparator;

/**
 * Created 15.05.14 by vlaaad
 */
public class Achievement {

    public static final Comparator<? super Achievement> COMPARATOR = new Comparator<Achievement>() {
        @Override public int compare(Achievement o1, Achievement o2) {
            return o1.order - o2.order;
        }
    };

    public final String name;
    public final String id;
    public final ObjectSet<EventType> eventTypes;
    public final AchievementCondition condition;
    public final int order;

    private boolean unlocked;

    public Achievement(String name, String id, ObjectSet<EventType> eventTypes, AchievementCondition condition, int order) {
        this.name = name;
        this.id = id;
        this.eventTypes = eventTypes;
        this.condition = condition;
        this.order = order;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void unlock() {
        unlocked = true;
    }

    public void load(Object o) {
        if (o instanceof Boolean) {
            unlocked = (Boolean) o;
            if (!unlocked) throw new IllegalStateException("loaded boolean, but was not true!");
            return;
        }
        if (o != null)
            condition.load(o);

    }

    public Object save() {
        if (unlocked)
            return true;
        return condition.save();
    }
}
