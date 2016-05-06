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

import com.vlaaad.common.util.*;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.imp.GiveExpResult;
import com.vlaaad.dice.game.actions.results.imp.SequenceResult;
import com.vlaaad.dice.game.actions.results.imp.TeleportResult;
import com.vlaaad.dice.game.actions.results.imp.TeleportTargetResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.util.ExpHelper;
import com.vlaaad.dice.game.world.World;

import java.util.Map;

/**
 * Created 18.04.14 by vlaaad
 */
public class TeleportTarget extends CreatureAction {

    private float radius;
    private float teleportRadius;

    public TeleportTarget(Ability owner) {
        super(owner);
    }

    @Override protected void doInit(Object setup) {
        Map map = (Map) setup;
        radius = MapHelper.get(map, "radius", Numbers.ONE).floatValue();
        teleportRadius = MapHelper.get(map, "teleport-radius", Numbers.ONE).floatValue();
        setDescriptionParamsMap(map);
    }

    @Override public IFuture<? extends IActionResult> apply(final Creature creature, World world) {
        return withCreature(
            creature,
            creatures(creature, Creature.CreatureRelation.anyExceptSelf, radius),
            new Function<Creature, IFuture<? extends IActionResult>>() {
                @Override public IFuture<? extends IActionResult> apply(final Creature target) {
                    return withCoordinate(
                        creature,
                        coordinates(target, teleportRadius, new ICondition<Grid2D.Coordinate>() {
                            @Override public boolean isSatisfied(Grid2D.Coordinate coordinate) {
                                return target.world.canStepTo(coordinate, target);
                            }
                        }),
                        new Function<Grid2D.Coordinate, IFuture<? extends IActionResult>>() {
                            @Override public IFuture<? extends IActionResult> apply(Grid2D.Coordinate coordinate) {
                                return Future.completed(new SequenceResult(
                                    new TeleportTargetResult(owner, creature, target, coordinate),
                                    new GiveExpResult(creature, ExpHelper.MIN_EXP)
                                ));
                            }
                        }
                    );
                }
            }
        );
    }
}
