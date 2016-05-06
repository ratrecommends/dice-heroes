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
import com.vlaaad.dice.game.world.players.Player;
import com.vlaaad.dice.pvp.messaging.IPvpMessage;
import com.vlaaad.dice.pvp.messaging.objects.PlacedCreature;

/**
 * Created 01.08.14 by vlaaad
 */
public class SpawnedToServer extends IPvpMessage {

    public static void register() {
        Config.json.addClassTag("sts", SpawnedToServer.class);
        Config.json.setSerializer(SpawnedToServer.class, new Json.Serializer<SpawnedToServer>() {
            @Override public void write(Json json, SpawnedToServer object, Class knownType) {
                json.writeObjectStart();
                if (knownType == null) json.writeType(SpawnedToServer.class);
                json.writeValue("d", object.dice, Array.class);
                json.writeValue("p", object.potions, ObjectMap.class, Integer.class);
                json.writeValue("packetIdx", object.packetIdx);
                json.writeObjectEnd();
            }
            @SuppressWarnings("unchecked")
            @Override public SpawnedToServer read(Json json, JsonValue jsonData, Class type) {
                Array<PlacedCreature> dice = json.readValue(Array.class, PlacedCreature.class, jsonData.get("d"));
                ObjectMap potions = json.readValue(ObjectMap.class, Integer.class, jsonData.get("p"));
                SpawnedToServer spawnedToServer = new SpawnedToServer(dice, potions);
                spawnedToServer.packetIdx = jsonData.getInt("packetIdx");
                return spawnedToServer;
            }
        });
    }


    public final Array<PlacedCreature> dice;
    public final ObjectMap<String, Integer> potions;

    public SpawnedToServer(Array<PlacedCreature> dice, ObjectMap<String, Integer> potions) {
        this.dice = dice;
        this.potions = potions;
    }
    public SpawnedToServer(Player player) {
        dice = new Array<PlacedCreature>(player.creatures.size);
        for (Creature creature : player.creatures) {
            dice.add(new PlacedCreature(creature));
        }
        potions = new ObjectMap<String, Integer>();
        for (Ability p : player.potions()) {
            potions.put(p.id, player.getPotionCount(p));
        }
    }
}
