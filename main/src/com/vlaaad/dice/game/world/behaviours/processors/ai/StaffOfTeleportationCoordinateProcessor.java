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

import com.vlaaad.common.util.Function;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCoordinatesParams;
import com.vlaaad.dice.game.world.util.DistanceFiller;

/**
 * Created 27.05.14 by vlaaad
 */
public class StaffOfTeleportationCoordinateProcessor extends AiAbilityCoordinateProcessor {
    public StaffOfTeleportationCoordinateProcessor() {
        super("staff-of-teleportation");
    }

    @Override protected Grid2D.Coordinate getResult(final AbilityCoordinatesParams params) {
        return AiDefaultTurnProcessor.selectBest(params.coordinates, new Function<Grid2D.Coordinate, Float>() {
            @Override public Float apply(Grid2D.Coordinate coordinate) {
                float r = 0;
                r += 1000 - DistanceFiller.getDistanceToNearCreatureOfRelation(
                    params.creature.world, coordinate.x(), coordinate.y(), params.creature, Creature.CreatureRelation.ally
                );
                return r;
            }
        });
    }
}
