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

package com.vlaaad.dice.game.config.levels;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.Iterator;

/**
 * Created 06.10.13 by vlaaad
 */
public class Levels implements Iterable<BaseLevelDescription> {

    private final ObjectMap<String, BaseLevelDescription> data;
    private final ObjectMap<Class, Array> byType = new ObjectMap<Class, Array>();

    public Levels(ObjectMap<String, BaseLevelDescription> data) {
        this.data = data;
    }

    public BaseLevelDescription get(String name) {
        if (!data.containsKey(name))
            throw new IllegalArgumentException("there is no such level: " + name);
        return data.get(name);
    }

    public Array<BaseLevelDescription> toArray() { return data.values().toArray(); }

    @Override public Iterator<BaseLevelDescription> iterator() {
        return data.values().iterator();
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseLevelDescription> Array<T> byType(Class<T> type) {
        if (byType.containsKey(type))
            return byType.get(type);
        Array<T> res = new Array<T>();
        for (BaseLevelDescription desc : data.values()) {
            if (type.isInstance(desc)) res.add((T) desc);
        }
        byType.put(type, res);
        return res;
    }
}
