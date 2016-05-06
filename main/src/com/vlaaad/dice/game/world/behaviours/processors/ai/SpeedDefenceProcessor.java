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

import com.badlogic.gdx.math.MathUtils;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.controllers.RandomController;
import com.vlaaad.dice.game.world.util.DistanceFiller;

/**
 * Created 12.02.14 by vlaaad
 */
public class SpeedDefenceProcessor extends AiDefaultProfessionAbilityProcessor {

    public SpeedDefenceProcessor() {
        super("warrior", "speed-defence");
    }

    @Override protected int preProcess(Creature creature, Ability ability) {
        if (creature.getCurrentLevel() == 6)
            return -1;
        int dst = DistanceFiller.getDistanceToNearCreatureOfRelation(
            creature.world,
            creature.getX(),
            creature.getY(),
            creature,
            Creature.CreatureRelation.enemy
        );
        if (dst == 3) {
            return creature.world.getController(RandomController.class).randomBoolean(0.75f) ? 2 : -1;
        }
        return -1;
    }
}
