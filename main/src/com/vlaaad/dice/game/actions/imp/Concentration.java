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

import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.results.imp.ConcentrationResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.behaviours.BehaviourRequest;
import com.vlaaad.dice.game.world.behaviours.params.AbilityAbilityParams;
import com.vlaaad.dice.game.world.controllers.BehaviourController;

/**
 * Created 04.02.14 by vlaaad
 */
public class Concentration extends CreatureAction {

    public Concentration(Ability owner) {
        super(owner);
    }

    @Override protected void doInit(Object setup) {
    }

    @Override public IFuture<ConcentrationResult> apply(final Creature creature, World world) {
        final Future<ConcentrationResult> future = new Future<ConcentrationResult>();
        world.getController(BehaviourController.class)
            .get(creature)
            .request(BehaviourRequest.ABILITY, new AbilityAbilityParams(owner, creature, creature.description.abilities()))
            .addListener(new IFutureListener<Ability>() {
                @Override public void onHappened(Ability ability) {
                    future.happen(new ConcentrationResult(creature, owner, ability));
                }
            });
        return future;
    }
}
