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

package com.vlaaad.dice.game.config.pvp;

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.Map;

/**
 * Created 26.07.14 by vlaaad
 */
public class PvpModes {
    private final ObjectMap<String, PvpMode> byName = new ObjectMap<String, PvpMode>();
    private final IntMap<PvpMode> byVariant = new IntMap<PvpMode>();

    public PvpModes(Iterable<Map> modes) {
        for (Map map : modes) {
            PvpMode mode = new PvpMode(map);
            if (byName.containsKey(mode.name))
                throw new IllegalStateException("mode with name " + mode.name + " already exists!");
            if (byVariant.containsKey(mode.variant))
                throw new IllegalStateException("mode with variant " + mode.variant + " already exists!");
            byName.put(mode.name, mode);
            byVariant.put(mode.variant, mode);
        }
    }
    public PvpMode get(String modeName) {
        if (!byName.containsKey(modeName))
            throw new IllegalStateException("there is no mode with name " + modeName);
        return byName.get(modeName);
    }

    public PvpMode get(int variant) {
        if (!byVariant.containsKey(variant))
            throw new IllegalStateException("there is no mode with variant " + variant);
        return byVariant.get(variant);
    }

}
