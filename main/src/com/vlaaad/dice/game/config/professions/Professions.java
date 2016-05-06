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

package com.vlaaad.dice.game.config.professions;

import com.badlogic.gdx.utils.ObjectMap;

import java.util.Iterator;

/**
 * Created 06.10.13 by vlaaad
 */
public class Professions implements Iterable<ProfessionDescription> {
    private final ObjectMap<String, ProfessionDescription> data;
    private final ObjectMap<String, ProfessionDescription> byId = new ObjectMap<String, ProfessionDescription>();

    public Professions(ObjectMap<String, ProfessionDescription> data) {
        this.data = data;
        for (ProfessionDescription a : data.values()) {
            ProfessionDescription prev = byId.put(a.id, a);
            if (prev != null) throw new IllegalStateException("Ids of " + prev.name + " and " + a.name + " clash: " + a.id);
        }
    }

    public ProfessionDescription get(String name) {
        if (!data.containsKey(name))
            throw new IllegalArgumentException("there is no profession with name " + name);
        return data.get(name);
    }

    public ProfessionDescription getById(String id) {
        ProfessionDescription ability = byId.get(id);
        if (ability == null) throw new IllegalStateException("there is no profession with id " + id);
        return ability;
    }

    @Override public Iterator<ProfessionDescription> iterator() {
        return data.values();
    }
}
