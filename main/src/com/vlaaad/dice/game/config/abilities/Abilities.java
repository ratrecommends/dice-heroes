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

package com.vlaaad.dice.game.config.abilities;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.util.Option;

import java.util.Iterator;

/**
 * Created 06.10.13 by vlaaad
 */
public class Abilities implements Iterable<Ability> {
    private final ObjectMap<String, Ability> data;
    private final ObjectMap<String, Ability> byId = new ObjectMap<String, Ability>();
    private final ObjectMap<Ability.Type, Array<Ability>> abilitiesByType = new ObjectMap<Ability.Type, Array<Ability>>();

    public Abilities(ObjectMap<String, Ability> data) {
        this.data = data;
        for (Ability a : data.values()) {
            Ability prev = byId.put(a.id, a);
            if (prev != null) throw new IllegalStateException("Ids of " + prev.name + " and " + a.name + " clash: " + a.id);
        }
    }

    public Ability get(String name) {
        if (!data.containsKey(name)) throw new IllegalArgumentException("there is no ability with name " + name);
        return data.get(name);
    }

    public Ability getById(String id) {
        Ability ability = byId.get(id);
        if (ability == null) throw new IllegalStateException("there is no ability with id " + id);
        return ability;
    }

    public Iterable<Ability> byType(Ability.Type type) {
        Array<Ability> res = abilitiesByType.get(type);
        if (res == null) {
            res = new Array<Ability>();
            for (Ability ability : this)
                if (ability.type == type) res.add(ability);
            abilitiesByType.put(type, res);
        }
        return res;
    }

    @Override public Iterator<Ability> iterator() {
        return data.values();
    }

    public Option<Ability> optional(String s) {
        return Option.option(data.get(s));
    }
}
