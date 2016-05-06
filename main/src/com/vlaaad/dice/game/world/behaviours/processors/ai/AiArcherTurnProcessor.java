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

import com.badlogic.gdx.math.Vector2;
import com.vlaaad.common.util.MathHelper;
import com.vlaaad.dice.game.actions.imp.Attack;
import com.vlaaad.dice.game.actions.imp.Shot;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.util.Coordinate;
import com.vlaaad.dice.game.world.util.DistanceFiller;

/**
 * Created 19.11.13 by vlaaad
 */
public class AiArcherTurnProcessor extends AiDefaultTurnProcessor {
    private static final Vector2 tmp1 = new Vector2();


    @Override public float getValue(Creature creature, int x, int y) {
        return getArcherValue(creature, x, y);
    }

    public static float getArcherValue(Creature creature, int x, int y) {
        World world = creature.world;
        int shotDistance = 2;
        for (Ability ability : creature.description.abilities) {
            if (ability != null && ability.action instanceof Shot) {
                Shot shot = (Shot) ability.action;
                shotDistance = Math.max(shotDistance, shot.distance);
            }
        }
        int nearEnemiesCount = Attack.findTargets(creature, Creature.CreatureRelation.enemy , x, y, world).size;
        float value = -nearEnemiesCount * 5000;
        int canAttackCount = Shot.findTargets(creature, Creature.CreatureRelation.enemy, x, y, world, shotDistance).size;
        value += canAttackCount * 2000;
        DistanceFiller.Result result = DistanceFiller.getInfoOfNearCreatureOfRelation(
            world,
            x,
            y,
            creature,
            Creature.CreatureRelation.enemy
        );

        if (result.distance != -1) {
            Coordinate nearest = result.position;
            float distanceToNearest = tmp1.set(x, y).dst(nearest.x, nearest.y);
            value += 500 * MathHelper.sign(distanceToNearest + 0.03125f - shotDistance) / distanceToNearest;
            value += 500 * MathHelper.sign(result.distance + 0.03125f - shotDistance) / result.distance;
        }
        return value;
    }
}
