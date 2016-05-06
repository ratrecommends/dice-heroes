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

package com.vlaaad.dice.game.config.items;

import com.badlogic.gdx.utils.ObjectMap;

import java.util.Iterator;

/**
 * Created 10.10.13 by vlaaad
 */
public class Items implements Iterable<Item> {
    private final ObjectMap<String, Item> data;
    private final ObjectMap<String, Item> byId = new ObjectMap<String, Item>();

    public Items(ObjectMap<String, Item> data) {
        this.data = data;
        for (Item a : data.values()) {
            if (a.id == null) throw new IllegalStateException("item " + a.name + " has no id!");
            Item prev = byId.put(a.id, a);
            if (prev != null) throw new IllegalStateException("Ids of " + prev.name + " and " + a.name + " clash: " + a.id);
        }
    }

    public Item get(String name) {
        if (!data.containsKey(name))
            throw new IllegalArgumentException("there is no item with name " + name);
        return data.get(name);
    }

    public Item getById(String name) {
        if (!byId.containsKey(name))
            throw new IllegalArgumentException("there is no item with id " + name);
        return byId.get(name);
    }

    @Override public Iterator<Item> iterator() {
        return data.values();
    }
}
