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
import com.vlaaad.dice.game.config.CreatureActionFactory;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.WorldObject;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.behaviours.BehaviourRequest;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCreatureParams;
import com.vlaaad.dice.game.world.controllers.BehaviourController;

import java.util.Map;

/**
 * Created 18.03.14 by vlaaad
 */
public class ApplyToTarget extends CreatureAction {

    private static final Vector2 tmp1 = new Vector2();

    private float radius;
    private Creature.CreatureRelation relation;
    private CreatureAction action;

    public ApplyToTarget(Ability owner) {
        super(owner);
    }

    @Override protected void doInit(Object setup) {
        Map map = (Map) setup;
        radius = MapHelper.get(map, "radius", Numbers.ONE).floatValue();
        relation = Creature.CreatureRelation.valueOf(MapHelper.get(map, "relation", "any"));
        action = CreatureActionFactory.createFromActionSetup(map, owner);
    }

    @Override public boolean canBeApplied(Creature creature, Thesaurus.LocalizationData reasonData) {
        return super.canBeApplied(creature, reasonData) && hasNearTargets(creature, reasonData);
    }

    private boolean hasNearTargets(Creature creature, Thesaurus.LocalizationData reasonData) {
        if (creature.world == null) {
            reasonData.key = "creature-is-not-on-map";
            reasonData.params = new Thesaurus.Params().with("die", creature.description.nameLocKey());
            return false;
        }
        if (findTargets(creature, relation, radius).size == 0) {
            reasonData.key = "ui-no-near-targets-for-action";
            return false;
        }
        return true;
    }

    @Override public IFuture<? extends IActionResult> apply(final Creature creature, World world) {
        Array<Creature> targets = findTargets(creature, relation, radius);
        if (targets.size == 0)
            return Future.completed(IActionResult.NOTHING);
        if (targets.size == 1)
            return action.apply(targets.first(), creature.world);
        final Future<IActionResult> future = new Future<IActionResult>();
        creature.world.getController(BehaviourController.class)
            .get(creature)
            .request(BehaviourRequest.CREATURE, new AbilityCreatureParams(creature, owner, targets))
            .addListener(new IFutureListener<Creature>() {
                @Override public void onHappened(Creature target) {
                    action.apply(target, creature.world).addListener(new IFutureListener<IActionResult>() {
                        @Override public void onHappened(IActionResult o) {
                            future.happen(o);
                        }
                    });
                }
            });
        return future;
    }

    public static Array<Creature> findTargets(Creature creature, Creature.CreatureRelation relation, float radius) {
        Vector2 creaturePos = tmp1.set(creature.getX(), creature.getY());
        Array<Creature> result = new Array<Creature>();
        for (WorldObject object : creature.world) {
            if (!(object instanceof Creature))
                continue;
            Creature check = (Creature) object;
            if (!check.get(Attribute.canBeSelected) || !creature.inRelation(relation, check) || check == creature)
                continue;
            if (creaturePos.dst(check.getX(), check.getY()) > radius)
                continue;
            result.add(check);
        }
        return result;
    }
}
