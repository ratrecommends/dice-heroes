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

import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.pvp.messaging.IPvpMessage;

/**
 * Created 28.07.14 by vlaaad
 */
public class Init extends IPvpMessage {

    public static void register() {
        Config.json.addClassTag("init", Init.class);
        Config.json.setElementType(Init.class, "fractions", String.class);
    }

    public int version;
    public String level;
    public ObjectMap<String, String> fractions;
    public int seed;

    public Init() {}

    public Init(String level, ObjectMap<String, String> fractions, int seed) {
        this.level = level;
        this.fractions = fractions;
        this.seed = seed;
        this.version = Config.mobileApi.getVersionCode();
    }
}
