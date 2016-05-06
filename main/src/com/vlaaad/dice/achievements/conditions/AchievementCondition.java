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

package com.vlaaad.dice.achievements.conditions;

import com.badlogic.gdx.utils.Array;
import com.vlaaad.dice.achievements.events.Event;
import com.vlaaad.dice.achievements.events.EventType;

/**
 * Created 15.05.14 by vlaaad
 */
public abstract class AchievementCondition<T extends Event> {

    public static final AchievementCondition ANY = new AchievementCondition() {
        @Override protected boolean satisfied(Event event) {
            return true;
        }
    };

    private final EventType<T> eventType;
    private final Class<T> eventClass;

    public AchievementCondition(EventType<T> eventType) {
        this.eventType = eventType;
        this.eventClass = eventType.eventClass;
    }

    public AchievementCondition(Class<T> eventClass) {
        this.eventType = null;
        this.eventClass = eventClass;
    }

    @SuppressWarnings("unchecked")
    protected AchievementCondition() {
        this.eventType = null;
        this.eventClass = (Class<T>) Event.class;
    }

    public void setup(Object params) {
    }

    public final boolean isSatisfied(Event event) {
        if (eventType != null && event.type() != eventType) {
            return false;
        }
        return eventClass.isInstance(event) && satisfied(eventClass.cast(event));
    }

    protected abstract boolean satisfied(T t);

    public void load(Object o) {
    }

    /**
     * @return anything that can be recognized by snake yaml, except booleans
     */
    public Object save() {
        return null;
    }


    public final Array<AchievementCondition> fill() {
        return fill(new Array<AchievementCondition>());
    }

    public final Array<AchievementCondition> fill(Array<AchievementCondition> target) {
        target.add(this);
        fillChildren(target);
        return target;
    }

    protected void fillChildren(Array<AchievementCondition> target) {
    }
}
