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

package com.vlaaad.dice.game.world.behaviours.processors.ai;

import com.vlaaad.common.util.Grid2D;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.actions.imp.Firestorm;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.WorldObject;
import com.vlaaad.dice.game.world.behaviours.RequestProcessor;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCoordinatesParams;

/**
 * Created 07.02.14 by vlaaad
 */
public class AiFirestormCoordinateProcessor extends RequestProcessor<Grid2D.Coordinate, AbilityCoordinatesParams> {
    @Override public int preProcess(AbilityCoordinatesParams params) {
        if (params.ability.action instanceof Firestorm)
            return 2;
        return -1;
    }

    @Override public IFuture<Grid2D.Coordinate> process(AbilityCoordinatesParams params) {
        Grid2D.Coordinate result = params.coordinates.first();
        int value = getNearEnemiesValue(result, params.creature);
        for (int i = 1; i < params.coordinates.size; i++) {
            Grid2D.Coordinate chk = params.coordinates.get(i);
            int chkValue = getNearEnemiesValue(chk, params.creature);
            if (chkValue > value) {
                value = chkValue;
                result = chk;
            }
        }
        return Future.completed(result);
    }

    public static int getNearEnemiesValue(Grid2D.Coordinate coordinate, Creature creature) {
        int r = 0;
        for (int i = coordinate.x() - 1; i <= coordinate.x() + 1; i++) {
            for (int j = coordinate.y() - 1; j <= coordinate.y() + 1; j++) {
                WorldObject object = creature.world.get(i, j);
                if (!(object instanceof Creature))
                    continue;
                Creature c = (Creature) object;
                if (creature.inRelation(Creature.CreatureRelation.ally, c))
                    r -= 10;
                else
                    r += 2;
                if (c.getX() == coordinate.x() && c.getY() == coordinate.y() && creature.inRelation(Creature.CreatureRelation.enemy, c)) {
                    r += 3;
                }
            }
        }
        return r;
    }
}
