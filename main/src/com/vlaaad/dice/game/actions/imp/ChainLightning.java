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
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.common.util.Numbers;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.game.actions.AttackType;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.imp.ChainLightningResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.WorldObject;
import com.vlaaad.dice.game.util.ExpHelper;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.behaviours.BehaviourRequest;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCreatureParams;
import com.vlaaad.dice.game.world.controllers.BehaviourController;
import com.vlaaad.dice.game.world.controllers.RandomController;

import java.util.Iterator;
import java.util.Map;

/**
 * Created 12.01.14 by vlaaad
 */
public class ChainLightning extends CreatureAction {

    private static final Vector2 tmp1 = new Vector2();
    private static final Vector2 tmp2 = new Vector2();
    private static final ObjectSet<Creature> tmpSet = new ObjectSet<Creature>();
    private static final Array<Creature> tmpArray2 = new Array<Creature>();
    private static final Array<Creature> tmpArray3 = new Array<Creature>();

    private int distance;
    private int targets;
    private int attackLevel;
    private AttackType attackType;


    public ChainLightning(Ability owner) {
        super(owner);
    }

    @Override protected void doInit(Object setup) {
        Map data = (Map) setup;
        distance = MapHelper.get(data, "distance", Numbers.ONE).intValue();
        targets = MapHelper.get(data, "targets", Numbers.ONE).intValue();
        attackLevel = MapHelper.get(data, "level", Numbers.ONE).intValue();
        attackType = AttackType.valueOf(MapHelper.get(data, "type", "weapon"));
    }

    @Override public IFuture<IActionResult> apply(final Creature creature, final World world) {
        Array<Creature> targets = findTargets(creature, Creature.CreatureRelation.enemy, creature.getX(), creature.getY(), world, distance);
        if (targets.size == 0) {
            return Future.completed(IActionResult.NOTHING);
        } else if (targets.size == 1) {
            return Future.completed(calcResult(owner, creature, targets.first(), world));
        } else {
            final Future<IActionResult> future = new Future<IActionResult>();
            world.getController(BehaviourController.class)
                .get(creature)
                .request(BehaviourRequest.CREATURE, new AbilityCreatureParams(creature, owner, targets))
                .addListener(new IFutureListener<Creature>() {
                    @Override public void onHappened(Creature target) {
                        future.happen(calcResult(owner, creature, target, world));
                    }
                });
            return future;
        }
    }

    private IActionResult calcResult(Ability ability, Creature caster, Creature target, World world) {
        ObjectSet<Creature> affected = tmpSet;
        Array<Creature> chain = new Array<Creature>();
        chain.add(target);
        affected.add(target);

        // i == 1 because we already have initial target
        RandomController random = world.getController(RandomController.class);

        for (int i = 1; i < targets; i++) {
            Creature last = chain.peek();
            tmpArray2.clear();
            Array<Creature> neighbours = getNeighbourCreatures(world, last.getX(), last.getY(), tmpArray2);
            Iterator<Creature> it = neighbours.iterator();
            while (it.hasNext()) {
                Creature toCheck = it.next();
                if (affected.contains(toCheck))
                    it.remove();
            }
            if (neighbours.size == 0) {
                break;
            } else if (neighbours.size == 1) {
                addToChain(affected, chain, neighbours.first());
            } else {
                Array<Creature> betterTargets = tmpArray3;
                betterTargets.clear();
                for (Creature toCheck : neighbours) {
                    if (toCheck.get(Attribute.defenceFor(attackType)) < attackLevel) {
                        betterTargets.add(toCheck);
                    }
                }
                if (betterTargets.size > 0) {
                    addToChain(affected, chain, random.random(betterTargets));
                } else {
                    addToChain(affected, chain, random.random(neighbours));
                }
            }
        }

        ObjectIntMap<Creature> expResults = new ObjectIntMap<Creature>();
        Array<Creature> killed = new Array<Creature>();

        for (Creature creature : chain) {
            if (creature.get(Attribute.defenceFor(attackType)) < attackLevel) { // killed
                if (creature.player != caster.player) {
                    expResults.getAndIncrement(caster, 0, ExpHelper.expForKill(caster, creature));
                }
                killed.add(creature);
            } else { // survived
                expResults.put(creature, ExpHelper.expForDefence(caster, creature));
            }
        }

        tmpSet.clear();
        tmpArray2.clear();
        tmpArray3.clear();

        return new ChainLightningResult(caster, ability, chain, killed, expResults);
    }

    private void addToChain(ObjectSet<Creature> affected, Array<Creature> chain, Creature toAdd) {
        if (!affected.add(toAdd))
            throw new IllegalStateException(toAdd + " should not be affected now!");
        chain.add(toAdd);
    }

    private Array<Creature> getNeighbourCreatures(World world, int x, int y, Array<Creature> creatures) {
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if (i == x && j == y)
                    continue;
                WorldObject object = world.get(i, j);
                if (object instanceof Creature && ((Creature) object).get(Attribute.canBeSelected)) {
                    creatures.add((Creature) object);
                }
            }
        }
        return creatures;
    }

    public static Array<Creature> findTargets(Creature creature, Creature.CreatureRelation relation, int x, int y, World world, int distance) {
        Vector2 creaturePos = tmp1.set(x, y);
        Array<Creature> result = new Array<Creature>();
        for (WorldObject object : world) {
            if (!(object instanceof Creature))
                continue;
            if (object.getX() == x && object.getY() == y)
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
