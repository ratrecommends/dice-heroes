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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.common.util.Numbers;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.imp.PoisonShotResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.effects.PoisonEffect;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.WorldObject;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.behaviours.BehaviourRequest;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCreatureParams;
import com.vlaaad.dice.game.world.controllers.BehaviourController;

import java.util.Map;

/**
 * Created 03.02.14 by vlaaad
 */
public class PoisonShot extends CreatureAction {

    private static final Vector2 tmp1 = new Vector2();
    private static final Vector2 tmp2 = new Vector2();

    private float radius;
    private int turnCount;

    public PoisonShot(Ability owner) {
        super(owner);
    }

    @Override protected void doInit(Object setup) {
        //{radius: 3, turns: 6}
        Map data = (Map) setup;
        radius = MapHelper.get(data, "radius", Numbers.ONE).floatValue();
        turnCount = MapHelper.get(data, "turns", Numbers.ONE).intValue();
    }

    @Override public boolean canBeApplied(Creature creature, Thesaurus.LocalizationData reasonData) {
        boolean res = super.canBeApplied(creature, reasonData);
        if (!res)
            return false;
        if (creature.world == null)
            return false;
        int targets = findTargets(creature, Creature.CreatureRelation.enemy, creature.getX(), creature.getY(), creature.world, radius).size;
        if (targets == 0) {
            reasonData.key = "ui-no-near-targets";
            return false;
        }
        return true;
    }

    @Override public IFuture<IActionResult> apply(final Creature creature, World world) {
        Array<Creature> targets = findTargets(creature, Creature.CreatureRelation.enemy, creature.getX(), creature.getY(), creature.world, radius);
        if (targets.size == 0)
            return Future.completed(IActionResult.NOTHING);
        if (targets.size == 1)
            return Future.completed(calcResult(creature, targets.first()));

        final Future<IActionResult> future = new Future<IActionResult>();
        world.getController(BehaviourController.class)
            .get(creature)
            .request(BehaviourRequest.CREATURE, new AbilityCreatureParams(creature, owner, targets))
            .addListener(new IFutureListener<Creature>() {
                @Override public void onHappened(Creature target) {
                    future.happen(calcResult(creature, target));
                }
            });
        return future;
    }

    private IActionResult calcResult(Creature creature, Creature target) {
        return new PoisonShotResult(creature, target, turnCount, owner);
    }

    public static Array<Creature> findTargets(Creature creature, Creature.CreatureRelation relation, int x, int y, World world, float distance) {
        Vector2 creaturePos = tmp1.set(x, y);
        Array<Creature> result = new Array<Creature>();
        for (WorldObject object : world) {
            if (!(object instanceof Creature))
                continue;
            Creature check = (Creature) object;
            if (!check.get(Attribute.canBeSelected) || !creature.inRelation(relation, check))
                continue;
            if (check.hasEffect(PoisonEffect.class))
                continue;
            Vector2 checkPos = tmp2.set(check.getX(), check.getY());
            if (checkPos.dst(creaturePos) > distance)
                continue;
            result.add(check);
        }
        return result;
    }
}
