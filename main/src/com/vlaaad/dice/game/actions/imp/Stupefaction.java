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
import com.vlaaad.dice.game.actions.results.imp.StupefactionResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.behaviours.BehaviourRequest;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCreatureParams;
import com.vlaaad.dice.game.world.controllers.BehaviourController;

import java.util.Map;

/**
 * Created 16.03.14 by vlaaad
 */
public class Stupefaction extends CreatureAction {

    private float radius;
    private int turnCount;

    public Stupefaction(Ability owner) {
        super(owner);
    }

    @Override protected void doInit(Object setup) {
        Map map = (Map) setup;
        radius = MapHelper.get(map, "radius", Numbers.ONE).floatValue();
        turnCount = MapHelper.get(map, "turns", Numbers.ONE).intValue();
    }

    @Override public boolean canBeApplied(Creature creature, Thesaurus.LocalizationData reasonData) {
        return super.canBeApplied(creature, reasonData) && TransformTargetToObstacle.hasTargetsNear(creature, reasonData, radius);
    }

    @Override public void fillDescriptionParams(Thesaurus.Params params, Creature creature) {
        params
            .with("radius", String.valueOf(radius))
            .with("turns", String.valueOf(turnCount));
    }

    @Override public IFuture<? extends IActionResult> apply(final Creature creature, World world) {
        Array<Creature> targets = Shot.findTargets(creature, Creature.CreatureRelation.enemy, radius);
        if (targets.size == 0) {
            return Future.completed(IActionResult.NOTHING);
        } else if (targets.size == 1) {
            return Future.completed(calcResult(creature, targets.first()));
        } else {
            final Future<IActionResult> future = new Future<IActionResult>();
            creature.world.getController(BehaviourController.class)
                .get(creature)
                .request(BehaviourRequest.CREATURE, new AbilityCreatureParams(creature, owner, targets))
                .addListener(new IFutureListener<Creature>() {
                    @Override public void onHappened(Creature target) {
                        future.happen(calcResult(creature, target));
                    }
                });
            return future;
        }
    }

    private IActionResult calcResult(Creature creature, Creature target) {
        return new StupefactionResult(owner, creature, target, turnCount);
    }
}
