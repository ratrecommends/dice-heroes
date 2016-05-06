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
import com.vlaaad.dice.game.actions.AttackType;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.imp.FireballResult;
import com.vlaaad.dice.game.actions.results.imp.GiveExpResult;
import com.vlaaad.dice.game.actions.results.imp.SequenceResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.WorldObject;
import com.vlaaad.dice.game.util.ExpHelper;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.behaviours.BehaviourRequest;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCreatureParams;
import com.vlaaad.dice.game.world.controllers.BehaviourController;

import java.util.HashMap;

/**
 * Created 09.01.14 by vlaaad
 */
public class Fireball extends CreatureAction {
    private static Vector2 tmp1 = new Vector2();
    private static Vector2 tmp2 = new Vector2();

    public AttackType attackType;
    public int attackLevel;
    public int distance;

    public Fireball(Ability owner) {
        super(owner);
    }

    @Override protected void doInit(Object setup) {
        HashMap data = (HashMap) setup;
        attackType = AttackType.valueOf(MapHelper.get(data, "type", "weapon"));
        attackLevel = MapHelper.get(data, "level", Numbers.ONE).intValue();
        distance = MapHelper.get(data, "distance", Numbers.ONE).intValue();
    }

    @Override public IFuture<IActionResult> apply(final Creature creature, World world) {
        Array<Creature> targets = findTargets(creature, Creature.CreatureRelation.enemy, creature.getX(), creature.getY(), world, distance);
        if (targets.size == 0)
            return Future.completed(IActionResult.NOTHING);
        else if (targets.size == 1) {
            return Future.completed(calcResult(owner, creature, targets.first(), attackType, attackLevel));
        } else {
            final Future<IActionResult> future = new Future<IActionResult>();
            world.getController(BehaviourController.class)
                .get(creature)
                .request(BehaviourRequest.CREATURE, new AbilityCreatureParams(creature, owner, targets))
                .addListener(new IFutureListener<Creature>() {
                    @Override public void onHappened(Creature target) {
                        future.happen(calcResult(owner, creature, target, attackType, attackLevel));
                    }
                });
            return future;
        }
    }

    public static IActionResult calcResult(Ability owner, Creature creature, Creature target, AttackType attackType, int attackLevel) {
        int defenceLevel = target.get(Attribute.<Integer>valueOf(attackType.toString() + Attribute.DEFENCE_SUFFIX));
        if (defenceLevel < attackLevel) { //success attack
            return new SequenceResult(
                new FireballResult(true, creature, target, attackType, attackLevel, owner),
                new GiveExpResult(creature, ExpHelper.expForKill(creature, target))
            );
        }
        return new SequenceResult(
            new FireballResult(false, creature, target, attackType, attackLevel, owner),
            new GiveExpResult(target, ExpHelper.expForDefence(creature, target))
        );
    }

    public static Array<Creature> findTargets(Creature creature, Creature.CreatureRelation relation, int x, int y, World world, int distance) {
        Vector2 creaturePos = tmp1.set(x, y);
        Array<Creature> result = new Array<Creature>();
        for (WorldObject object : world) {
            if (!(object instanceof Creature))
                continue;
            Creature check = (Creature) object;
            if (!check.get(Attribute.canBeSelected) || !creature.inRelation(relation, check))
                continue;
            Vector2 checkPos = tmp2.set(check.getX(), check.getY());
            if (checkPos.dst(creaturePos) > distance)
                continue;
            result.add(check);
        }
        return result;
    }
}
