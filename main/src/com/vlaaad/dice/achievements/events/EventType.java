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

package com.vlaaad.dice.achievements.events;

import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.dice.achievements.events.imp.*;

/**
 * Created 16.05.14 by vlaaad
 */
public final class EventType<T extends Event> {

    private static final ObjectMap<String, EventType> values = new ObjectMap<String, EventType>();

    public static EventType valueOf(String name) {
        EventType result = values.get(name);
        if (result == null)
            throw new IllegalStateException("There is no such event type: " + name);
        return result;
    }

    public static final EventType<Event> startup = new EventType<Event>("startup", Event.class);
    public static final EventType<DieEvent> obtainDie = new EventType<DieEvent>("obtain-die", DieEvent.class);
    public static final EventType<EarnEvent> earnItem = new EventType<EarnEvent>("earn-item", EarnEvent.class);
    public static final EventType<BrewEvent> brewPotion = new EventType<BrewEvent>("brew-potion", BrewEvent.class);
    public static final EventType<EndLevelEvent> endLevel = new EventType<EndLevelEvent>("end-level", EndLevelEvent.class);
    public static final EventType<KillEvent> kill = new EventType<KillEvent>("kill", KillEvent.class);
    public static final EventType<Event> aboutWindow = new EventType<Event>("about-window", Event.class);
    public static final EventType<Event> donated = new EventType<Event>("donated", Event.class);

    public final String name;
    public final Class<T> eventClass;

    private EventType(String name, Class<T> eventClass) {
        this.name = name;
        this.eventClass = eventClass;
        if (values.put(name, this) != null)
            throw new IllegalStateException("there is already event type with name " + name);
    }

    @Override public String toString() {
        return name;
    }
}
