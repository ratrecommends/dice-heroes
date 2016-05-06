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

import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.common.util.Numbers;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.levels.BaseLevelDescription;
import com.vlaaad.dice.game.config.levels.LevelDescription;

import java.util.List;
import java.util.Map;

/**
 * Created 26.07.14 by vlaaad
 */
public class PvpMode {

    public static enum Type {
        friends, global
    }

    public final BaseLevelDescription metaLevel;
    public final String name;
    public final int variant;
    public final int players;
    public final Type type;
    public final Array<LevelDescription> levels;

    public PvpMode(Map params) {
        this.name = MapHelper.get(params, "name");
        this.variant = MapHelper.get(params, "variant", Numbers.ZERO).intValue();
        this.type = Type.valueOf(MapHelper.get(params, "type", "friends"));
        this.players = MapHelper.get(params, "players", Numbers.TWO).intValue();
        //meta-level: friends-pvp
        this.metaLevel = Config.levels.get(MapHelper.get(params, "meta-level", "not-specified"));
        List<String> names = MapHelper.get(params, "levels");
        this.levels = new Array<LevelDescription>(names.size());
        for (String name : names) {
            levels.add((LevelDescription) Config.levels.get(name));
        }
    }

    @Override public String toString() {
        return "PvpMode{" +
            "name='" + name + '\'' +
            ", variant=" + variant +
            ", players=" + players +
            ", type=" + type +
            ", levels=" + levels +
            '}';
    }
}
