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

package com.vlaaad.dice.pvp.messaging.objects;

import com.badlogic.gdx.utils.*;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.objects.Creature;

/**
 * Created 29.07.14 by vlaaad
 */
public class PlacedCreature {

    public static void register() {
        Config.json.addClassTag("pc", PlacedCreature.class);
        Config.json.setSerializer(PlacedCreature.class, new Json.Serializer<PlacedCreature>() {
            @Override public void write(Json json, PlacedCreature object, Class knownType) {
                json.writeArrayStart();
                json.writeValue(object.name);
                json.writeValue(object.profession);
                json.writeValue(object.exp);
                json.writeValue(object.x);
                json.writeValue(object.y);
                json.writeValue(object.id);
                json.writeValue(object.abilities, Array.class, String.class);
                json.writeValue(object.drop, ObjectMap.class, Integer.class);
                json.writeArrayEnd();
            }

            @SuppressWarnings("unchecked")
            @Override public PlacedCreature read(Json json, JsonValue jsonData, Class type) {
                return new PlacedCreature(
                    jsonData.getString(0),
                    jsonData.getString(1),
                    jsonData.getInt(2),
                    jsonData.getInt(3),
                    jsonData.getInt(4),
                    jsonData.getString(5),
                    json.readValue(Array.class, String.class, jsonData.get(6)),
                    json.readValue(ObjectMap.class, Integer.class, jsonData.get(7))
                );
            }
        });
    }

    public final String name;
    public final String profession;
    public final int exp;
    public final int x;
    public final int y;
    public final String id;
    public final Array<String> abilities = new Array<String>(6);
    public final ObjectMap<String, Integer> drop = new ObjectMap<String, Integer>(0);

    public PlacedCreature(String name, String profession, int exp, int x, int y, String id, Array<String> abilities, ObjectMap<String, Integer> drop) {
        this.name = name;
        this.profession = profession;
        this.exp = exp;
        this.x = x;
        this.y = y;
        this.id = id;
        this.abilities.addAll(abilities);
        this.drop.putAll(drop);
    }

    public PlacedCreature(Creature creature) {
        id = creature.id;
        name = creature.description.name;
        profession = creature.description.profession.id;
        exp = creature.description.exp;
        x = creature.getX();
        y = creature.getY();
        for (Ability a : creature.abilities) {
            this.abilities.add(a.id);
        }
        for (ObjectIntMap.Entry<Item> e : creature.drop.entries()) {
            drop.put(e.key.id, e.value);
        }
    }
}
