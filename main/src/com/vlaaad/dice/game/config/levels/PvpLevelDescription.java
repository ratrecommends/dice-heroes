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

package com.vlaaad.dice.game.config.levels;

import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.common.util.Numbers;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.pvp.PvpMode;

import java.util.List;
import java.util.Map;

/**
 * Created 23.07.14 by vlaaad
 */
public class PvpLevelDescription extends BaseLevelDescription {

    public final int players;
    private final String modeName;
    private final List<String> levelNames;

    private Array<LevelDescription> levels;
    private PvpMode mode;

    public PvpLevelDescription(Map data) {
        super(data);
        players = MapHelper.get(data, "players", Numbers.TWO).intValue();
        levelNames = MapHelper.get(data, "levels");
        modeName = MapHelper.get(data, "mode", "friends-2");
    }

    public PvpMode getMode() {
        if (mode == null) {
            mode = Config.pvpModes.get(modeName);
        }
        return mode;
    }

    public Array<LevelDescription> getLevels() {
        if (levels == null) {
            levels = new Array<LevelDescription>(false, levelNames.size());
            for (String levelName : levelNames) {
                levels.add((LevelDescription) Config.levels.get(levelName));
            }
        }
        return levels;
    }
}
