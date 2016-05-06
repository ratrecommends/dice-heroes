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

package com.vlaaad.common.util;

import com.badlogic.gdx.utils.ObjectIntMap;
import com.vlaaad.dice.game.config.abilities.Ability;

import java.util.Map;

/**
 * Created 13.10.13 by vlaaad
 */
public class MapHelper {
    private MapHelper() {
    }

    public static <T> T get(Map data, Object key) {
        return get(data, key, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Map data, Object key, T defaultValue) {
        Object o = data.get(key);
        if (o == null)
            return defaultValue;
        return (T) o;
    }

    public static boolean isEmpty(ObjectIntMap<?> potions) {
        ObjectIntMap.Values values = potions.values();
        while (values.hasNext()) {
            if (values.next() != 0)
                return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public static Iterable<? extends String> keys(Map map) {
        return map.keySet();
    }

    public static int countPositive(ObjectIntMap<?> items) {
        int i = 0;
        for (ObjectIntMap.Entry<?> entry : items) {
            if (entry.value > 0) i++;
        }
        return i;
    }
}
