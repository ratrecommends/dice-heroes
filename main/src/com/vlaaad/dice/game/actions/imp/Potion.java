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

import com.vlaaad.common.util.MapHelper;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.imp.PotionResult;
import com.vlaaad.dice.game.config.CreatureActionFactory;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;

import java.util.Map;

/**
 * Created 07.03.14 by vlaaad
 */
public class Potion extends CreatureAction {

    public static enum ActionType {
        drink, throwToCreature
    }

    public CreatureAction throwToCreature;
    public CreatureAction drink;

    public Potion(Ability owner) {
        super(owner);
    }

    @Override protected void doInit(Object setup) {
        Map data = (Map) setup;
        Map drink = MapHelper.get(data, "drink");
        if (drink != null) {
            this.drink = CreatureActionFactory.createFromActionSetup(drink, owner);
        }
        Map throwToCreature = MapHelper.get(data, "throw");
        if (throwToCreature != null) {
            this.throwToCreature = CreatureActionFactory.createFromActionSetup(throwToCreature, owner);
        }
    }

    @Override public IFuture<? extends IActionResult> apply(Creature creature, World world) {
        if (drink == null && throwToCreature == null) {
            return Future.completed(IActionResult.NOTHING);
        } else if (drink == null) {
            return apply(owner, ActionType.throwToCreature, throwToCreature, creature, world);
        } else if (throwToCreature == null) {
            return apply(owner, ActionType.drink, drink, creature, world);
        } else {
            ActionType actionType = creature.get(Attribute.potionAction);
            creature.set(Attribute.potionAction, null);
            if (actionType == ActionType.drink) {
                return apply(owner, ActionType.drink, drink, creature, world);
            } else if (actionType == ActionType.throwToCreature) {
                return apply(owner, ActionType.throwToCreature, throwToCreature, creature, world);
            } else {
                throw new RuntimeException("Not implemented: select between drink and throw");
            }
        }
    }

    @Override public void fillDescriptionParams(Thesaurus.Params params, Creature creature) {
        if (drink != null) {
            drink.fillDescriptionParams(params, creature);
        }
        if (throwToCreature != null) {
            throwToCreature.fillDescriptionParams(params, creature);
        }
    }

    private static IFuture<? extends IActionResult> apply(final Ability owner, final ActionType actionType, CreatureAction action, final Creature creature, World world) {
        final Future<IActionResult> future = new Future<IActionResult>();
        action.apply(creature, world).addListener(new IFutureListener<IActionResult>() {
            @Override public void onHappened(IActionResult result) {
                future.happen(new PotionResult(creature, owner, actionType, result));
            }
        });
        return future;
    }
}
