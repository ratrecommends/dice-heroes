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
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.util.ExpHelper;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCreatureParams;

/**
 * Created 27.05.14 by vlaaad
 */
public class StaffOfTeleportationCreatureProcessor extends AiAbilityCreatureProcessor {

    public StaffOfTeleportationCreatureProcessor() {
        super("staff-of-teleportation");
    }

    @Override protected Creature getResult(final AbilityCreatureParams params) {
        return AiDefaultTurnProcessor.selectBest(params.available, new Function<Creature, Float>() {
            @Override public Float apply(Creature that) {
                float r = 0;
                if (that.inRelation(Creature.CreatureRelation.enemy, params.creature))
                    r += ExpHelper.getTotalCost(that);
                return r;
            }
        });
    }
}
