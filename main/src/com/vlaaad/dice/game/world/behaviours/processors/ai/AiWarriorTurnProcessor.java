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

import com.badlogic.gdx.utils.Array;
import com.vlaaad.dice.game.actions.imp.Attack;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.WorldObject;
import com.vlaaad.dice.game.world.util.AStar;
import com.vlaaad.dice.game.world.util.Coordinate;

/**
 * Created 08.10.13 by vlaaad
 */
public class AiWarriorTurnProcessor extends AiDefaultTurnProcessor {


    @Override public float getValue(Creature creature, int x, int y) {
        return getWarriorValue(creature, x, y);
    }

    public static float getWarriorValue(Creature creature, int x, int y) {
        float value = Attack.findTargets(creature, creature.world).size * 1000;
        if (creature.getX() == x && creature.getY() == y)
            value += 100;
        for (WorldObject o : creature.world) {
            if (o instanceof Creature) {
                Creature c = (Creature) o;
                if (!creature.inRelation(Creature.CreatureRelation.enemy, c))
                    continue;
                Array<Coordinate> result = AStar.search(creature.world, creature, c, true);
                if (result == null || result.size == 0)
                    continue;
                if (result.first().x == x && result.first().y == y) {
                    float pathValue = 30 - result.size;
                    pathValue = pathValue < 0 ? 0 : pathValue;
                    pathValue *= 200;
                    value += pathValue;
                }
                Coordinate.freeAll(result);
            }
        }
        return value;
    }
}
