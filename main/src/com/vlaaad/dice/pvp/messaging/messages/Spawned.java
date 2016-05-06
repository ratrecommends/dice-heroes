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

package com.vlaaad.dice.pvp.messaging.messages;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.players.Fraction;
import com.vlaaad.dice.game.world.players.Player;
import com.vlaaad.dice.pvp.messaging.IPvpMessage;
import com.vlaaad.dice.pvp.messaging.objects.PlacedCreature;

/**
 * Created 29.07.14 by vlaaad
 */
public class Spawned extends IPvpMessage {

    public static void register() {
        Config.json.addClassTag("spawned", Spawned.class);
        Config.json.setSerializer(Spawned.class, new Json.Serializer<Spawned>() {
            @Override public void write(Json json, Spawned object, Class knownType) {
                json.writeObjectStart();
                if (knownType == null) json.writeType(Spawned.class);
                json.writeValue("d", object.dice, Array.class);
                json.writeValue("p", object.potions, ObjectMap.class, Integer.class);
                json.writeValue("f", object.fraction.name, String.class);
                json.writeValue("packetIdx", object.packetIdx);
                json.writeObjectEnd();
            }
            @SuppressWarnings("unchecked")
            @Override public Spawned read(Json json, JsonValue jsonData, Class type) {
                Array<PlacedCreature> dice = json.readValue(Array.class, PlacedCreature.class, jsonData.get("d"));
                ObjectMap potions = json.readValue(ObjectMap.class, Integer.class, jsonData.get("p"));
                Fraction fraction = Fraction.valueOf(jsonData.getString("f"));
                Spawned spawned = new Spawned(dice, potions, fraction);
                spawned.packetIdx = jsonData.getInt("packetIdx");
                return spawned;
            }
        });
    }


    public final Array<PlacedCreature> dice;
    public final ObjectMap<String, Integer> potions;
    public final Fraction fraction;

    public Spawned(Array<PlacedCreature> dice, ObjectMap<String, Integer> potions, Fraction fraction) {
        this.dice = dice;
        this.potions = potions;
        this.fraction = fraction;
    }
    public Spawned(Player player) {
        dice = new Array<PlacedCreature>(player.creatures.size);
        for (Creature creature : player.creatures) {
            dice.add(new PlacedCreature(creature));
        }
        potions = new ObjectMap<String, Integer>();
        for (Ability p : player.potions()) {
            potions.put(p.id, player.getPotionCount(p));
        }
        fraction = player.fraction;
    }
}
