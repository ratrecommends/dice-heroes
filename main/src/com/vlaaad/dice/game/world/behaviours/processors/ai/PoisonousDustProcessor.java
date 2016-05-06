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
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.WorldObject;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCoordinatesParams;

/**
 * Created 27.05.14 by vlaaad
 */
public class PoisonousDustProcessor extends AiAbilityCoordinateProcessor {

    public PoisonousDustProcessor() {
        super("poisonous-dust");
    }

    @Override protected Grid2D.Coordinate getResult(final AbilityCoordinatesParams params) {
        return AiDefaultTurnProcessor.selectBest(params.coordinates, new Function<Grid2D.Coordinate, Float>() {
            @Override public Float apply(Grid2D.Coordinate coordinate) {
                float r = 0;
                for (int i = coordinate.x() - 1; i <= coordinate.x() + 1; i++) {
                    for (int j = coordinate.y() - 1; j <= coordinate.y() + 1; j++) {
                        WorldObject object = params.creature.world.get(i, j);
                        if (!(object instanceof Creature))
                            continue;
                        Creature c = (Creature) object;
                        if ((params.creature.inRelation(Creature.CreatureRelation.ally, c)) && !c.get(Attribute.poisoned))
                            r -= 8;
                        else
                            r += 2;
                        if (c.getX() == coordinate.x() && c.getY() == coordinate.y() && params.creature.inRelation(Creature.CreatureRelation.enemy, c)) {
                            r += 1;
                        }
                    }
                }
                return r;
            }
        });
    }
}
