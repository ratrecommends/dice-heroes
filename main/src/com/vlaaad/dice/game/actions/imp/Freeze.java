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
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.common.util.Numbers;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.imp.FreezeResult;
import com.vlaaad.dice.game.actions.results.imp.GiveExpResult;
import com.vlaaad.dice.game.actions.results.imp.SequenceResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.util.ExpHelper;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.behaviours.BehaviourRequest;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCreatureParams;
import com.vlaaad.dice.game.world.controllers.BehaviourController;

import java.util.Iterator;
import java.util.Map;

/**
 * Created 10.01.14 by vlaaad
 */
public class Freeze extends CreatureAction {

    public float distance;
    public int turns;

    public Freeze(Ability owner) {
        super(owner);
    }

    @Override protected void doInit(Object setup) {
        Map data = (Map) setup;
        distance = MapHelper.get(data, "distance", Numbers.ONE).floatValue();
        turns = MapHelper.get(data, "turns", Numbers.ONE).intValue();
    }

    @Override public IFuture<IActionResult> apply(final Creature creature, World world) {
        Array<Creature> targets = Shot.findTargets(creature, Creature.CreatureRelation.enemy, creature.getX(), creature.getY(), world, distance);
        Iterator<Creature> it = targets.iterator();
        while (it.hasNext()) {
            if (it.next().get(Attribute.frozen)) {
                it.remove();
            }
        }
        if (targets.size == 0)
            return Future.completed(IActionResult.NOTHING);
        else if (targets.size == 1)
            return Future.<IActionResult>completed(new FreezeResult(owner, creature, targets.first(), turns));
        else {
            final Future<IActionResult> future = new Future<IActionResult>();
            world.getController(BehaviourController.class)
                .get(creature)
                .request(BehaviourRequest.CREATURE, new AbilityCreatureParams(creature, owner, targets))
                .addListener(new IFutureListener<Creature>() {
                    @Override public void onHappened(Creature target) {
                        future.happen(new SequenceResult(
                            new FreezeResult(owner, creature, target, turns),
                            new GiveExpResult(creature, ExpHelper.MIN_EXP)
                        ));
                    }
                });
            return future;
        }
    }
}
