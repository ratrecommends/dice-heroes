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
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.game.actions.AttackType;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.imp.BerserkAttackResult;
import com.vlaaad.dice.game.actions.results.imp.GiveExpResult;
import com.vlaaad.dice.game.actions.results.imp.SequenceResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.WorldObject;
import com.vlaaad.dice.game.util.ExpHelper;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.behaviours.BehaviourRequest;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCreatureParams;
import com.vlaaad.dice.game.world.controllers.BehaviourController;

import java.util.HashMap;

/**
 * Created 08.02.14 by vlaaad
 */
public class BerserkAttack extends CreatureAction {

    public AttackType attackType;
    public int attackLevel;

    public BerserkAttack(Ability owner) {
        super(owner);
    }

    @Override
    protected void doInit(Object setup) {
        HashMap data = (HashMap) setup;
        attackType = AttackType.valueOf((String) data.get("type"));
        attackLevel = ((Number) data.get("level")).intValue();
    }

    @Override public boolean canBeApplied(Creature creature, Thesaurus.LocalizationData reasonData) {
        return super.canBeApplied(creature, reasonData) && hasEnemiesNear(creature, reasonData);
    }

    private boolean hasEnemiesNear(Creature creature, Thesaurus.LocalizationData reasonData) {
        if (creature.world == null) {
            reasonData.key = "creature-is-not-on-map";
            reasonData.params = new Thesaurus.Params().with("die", creature.description.nameLocKey());
            return false;
        }
        if (findTargets(creature, creature.world).size > 0)
            return true;
        reasonData.key = "no-near-enemies";
        reasonData.params = new Thesaurus.Params().with("die", creature.description.nameLocKey());
        return false;
    }

    @Override
    public IFuture<IActionResult> apply(final Creature creature, World world) {
        Array<Creature> targets = findTargets(creature, world);
        if (targets.size == 0) {
            return Future.completed(IActionResult.NOTHING);
        } else if (targets.size == 1) {
            return Future.completed(calcResult(owner, creature, targets.first(), attackType, attackLevel));
        } else {
            final Future<IActionResult> future = new Future<IActionResult>();
            world.getController(BehaviourController.class).get(creature).request(BehaviourRequest.CREATURE, new AbilityCreatureParams(creature, this.owner, targets)).addListener(new IFutureListener<Creature>() {
                @Override public void onHappened(Creature target) {
                    future.happen(calcResult(owner, creature, target, attackType, attackLevel));
                }
            });
            return future;
        }
    }

    public static IActionResult calcResult(Ability owner, Creature creature, Creature target, AttackType attackType, int attackLevel) {
        int defenceLevel = target.get(Attribute.<Integer>valueOf(attackType.toString() + Attribute.DEFENCE_SUFFIX));
        creature.set(Attribute.attackFor(attackType), attackLevel);
        int realAttackLevel = creature.get(Attribute.attackFor(attackType));
        if (defenceLevel < realAttackLevel) { //success attack
            return new SequenceResult(
                new BerserkAttackResult(true, creature, target, attackType, attackLevel, owner),
                new GiveExpResult(creature, ExpHelper.expForKill(creature, target))
            );
        }
        return new SequenceResult(
            new BerserkAttackResult(false, creature, target, attackType, attackLevel, owner),
            new GiveExpResult(target, ExpHelper.expForDefence(creature, target))
        );
    }

    public static Array<Creature> findTargets(Creature creature, World world) {
        return findTargets(creature, Creature.CreatureRelation.enemy, creature.getX(), creature.getY(), world);
    }

    public static Array<Creature> findTargets(Creature creature, Creature.CreatureRelation relation, int x, int y, World world) {
        Array<Creature> result = new Array<Creature>();
        addEnemyOf(result, creature, relation, world, x, y + 1);

        addEnemyOf(result, creature, relation, world, x - 1, y + 1);
        addEnemyOf(result, creature, relation, world, x - 1, y);
        addEnemyOf(result, creature, relation, world, x - 1, y - 1);

        addEnemyOf(result, creature, relation, world, x, y - 1);

        addEnemyOf(result, creature, relation, world, x + 1, y - 1);
        addEnemyOf(result, creature, relation, world, x + 1, y);
        addEnemyOf(result, creature, relation, world, x + 1, y + 1);

        return result;
    }

    private static void addEnemyOf(Array<Creature> targets, Creature creature, Creature.CreatureRelation relation, World world, int x, int y) {
        WorldObject object = world.get(x, y);
        if (object instanceof Creature) {
            Creature c = (Creature) object;
            if (c.get(Attribute.canBeSelected) && creature.inRelation(relation, c)) {
                targets.add(c);
            }
        }
    }
}
