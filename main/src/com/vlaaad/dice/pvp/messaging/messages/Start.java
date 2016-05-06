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
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.pvp.messaging.IPvpMessage;

/**
 * Created 29.07.14 by vlaaad
 */
public class Start extends IPvpMessage {

    public static void register() {
        Config.json.addClassTag("start", Start.class);
        Config.json.addClassTag("coordinate", Grid2D.Coordinate.class);
        Config.json.setElementType(Start.class, "order", String.class);
        Config.json.setSerializer(Grid2D.Coordinate.class, new Json.Serializer<Grid2D.Coordinate>() {
            @Override public void write(Json json, Grid2D.Coordinate object, Class knownType) {
                json.writeArrayStart();
                json.writeValue(object.x());
                json.writeValue(object.y());
                json.writeArrayEnd();
            }
            @Override public Grid2D.Coordinate read(Json json, JsonValue jsonData, Class type) {
                return new Grid2D.Coordinate(jsonData.getInt(0), jsonData.getInt(1));
            }
        });
    }

    public Array<String> order;
    public Start() {}
    public Start(Array<? extends Creature> creatures) {
        order = new Array<String>();
        for (Creature c : creatures) {
            order.add(c.id);
        }
    }
}
