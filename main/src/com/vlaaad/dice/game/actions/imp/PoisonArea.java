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

package com.vlaaad.dice.game.actions.imp;

import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.util.*;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.imp.GiveExpResult;
import com.vlaaad.dice.game.actions.results.imp.PoisonAreaResult;
import com.vlaaad.dice.game.actions.results.imp.SequenceResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.levels.LevelElementType;
import com.vlaaad.dice.game.effects.PoisonEffect;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;

import java.util.Map;

/**
 * Created 20.04.14 by vlaaad
 */
public class PoisonArea extends CreatureAction {

    private static final Function<Creature, Boolean> CONDITION = new Function<Creature, Boolean>() {
        @Override public Boolean apply(Creature creature) {
            return !creature.hasEffect(PoisonEffect.class);
        }
    };
    private float radius;
    private float area;
    private int turns;

    public PoisonArea(Ability owner) {
        super(owner);
    }

    @Override protected void doInit(Object setup) {
        //{radius: 6, area: 1.5, turns: 4}
        Map map = (Map) setup;
        setDescriptionParamsMap(map);
        radius = MapHelper.get(map, "radius", Numbers.ONE).floatValue();
        area = MapHelper.get(map, "area", Numbers.ONE).floatValue();
        turns = MapHelper.get(map, "turns", Numbers.ONE).intValue();
    }

    @Override public IFuture<? extends IActionResult> apply(final Creature creature, final World world) {
        return withCoordinate(
            creature,
            coordinates(
                creature,
                radius,
                new ICondition<Grid2D.Coordinate>() {
                    @Override public boolean isSatisfied(Grid2D.Coordinate coordinate) {
                        return creature.world.level.exists(LevelElementType.tile, coordinate.x(), coordinate.y());
                    }
                }
            ),
            new Function<Grid2D.Coordinate, IFuture<? extends IActionResult>>() {
                @Override public IFuture<? extends IActionResult> apply(Grid2D.Coordinate coordinate) {
                    return Future.completed(calcResult(creature, coordinate));
                }
            }
        );
    }

    private IActionResult calcResult(Creature creature, Grid2D.Coordinate coordinate) {
        Array<Creature> creatures = creatures(creature.world, coordinate.x(), coordinate.y(), CONDITION, area);
        int enemies = 0;
        for (Creature c : creatures) {
            if (creature.inRelation(Creature.CreatureRelation.enemy, c))
                enemies += 0;
        }
        return new SequenceResult(
            new PoisonAreaResult(owner, creature, coordinate, creatures, turns),
            new GiveExpResult(creature, enemies)
        );
    }
}
