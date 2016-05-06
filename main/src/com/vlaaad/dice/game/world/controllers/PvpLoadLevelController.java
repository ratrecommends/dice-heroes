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

package com.vlaaad.dice.game.world.controllers;

import com.vlaaad.common.util.Grid2D;
import com.vlaaad.dice.game.config.levels.LevelDescription;
import com.vlaaad.dice.game.config.levels.LevelElementType;
import com.vlaaad.dice.game.objects.Obstacle;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.WorldController;
import com.vlaaad.dice.game.world.players.Fraction;
import com.vlaaad.dice.game.world.view.SpawnPoint;
import com.vlaaad.dice.game.world.view.TileInfo;

import java.util.Map;

/**
 * Created 29.07.14 by vlaaad
 */
public class PvpLoadLevelController extends WorldController {
    public PvpLoadLevelController(World world) {
        super(world);
    }
    @Override protected void start() {
        LevelDescription description = world.level;
        for (Map.Entry<Grid2D.Coordinate, String> entry : description.getElements(LevelElementType.tile)) {
            world.dispatcher.dispatch(PveLoadLevelController.LOAD_TILE, new TileInfo(entry.getKey().x(), entry.getKey().y(), entry.getValue()));
        }
        for (Map.Entry<Grid2D.Coordinate, Obstacle> entry : description.getElements(LevelElementType.obstacle)) {
            world.add(entry.getKey().x(), entry.getKey().y(), entry.getValue());
        }
        for (Map.Entry<Grid2D.Coordinate, Fraction> entry : description.getElements(LevelElementType.spawn)) {
            world.dispatcher.dispatch(PveLoadLevelController.ADD_SPAWN_POINT, new SpawnPoint(entry.getKey().x(), entry.getKey().y(), entry.getValue()));
        }
        world.dispatcher.dispatch(PveLoadLevelController.LEVEL_LOADED, null);
    }
    @Override protected void stop() {

    }
}
