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
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.config.CreatureActionFactory;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.controllers.ViewController;

import java.util.Map;

/**
 * Created 08.01.14 by vlaaad
 */
public class Sequence extends CreatureAction {

    private final Array<CreatureAction> actions = new Array<CreatureAction>();

    public Sequence(Ability owner) {
        super(owner);
    }

    @SuppressWarnings("unchecked")
    @Override protected void doInit(Object setup) {
        Iterable<Map> list = (Iterable<Map>) setup;
        for (Map data : list) {
            String action = MapHelper.get(data, "action");
            Object actionSetup = MapHelper.get(data, "setup");
            actions.add(CreatureActionFactory.create(action, actionSetup, owner));
        }
    }

    @Override public boolean canBeApplied(Creature creature, Thesaurus.LocalizationData reasonData) {
        for (CreatureAction action : actions) {
            if (!action.canBeApplied(creature, reasonData)) {
                return false;
            }
        }
        return true;
    }

    @Override public void fillDescriptionParams(Thesaurus.Params params, Creature creature) {
        for (CreatureAction action : actions) {
            action.fillDescriptionParams(params, creature);
        }
    }

    @Override public IFuture<IActionResult> apply(Creature creature, World world) {
        final Future<IActionResult> future = new Future<IActionResult>();
        apply(future, creature, world, 0);
        return future;
    }

    private void apply(final Future<IActionResult> future, final Creature creature, final World world, final int idx) {
        if (idx >= actions.size) {
            future.happen(IActionResult.NOTHING);
            return;
        }
        CreatureAction action = actions.get(idx);
        action.apply(creature, world).addListener(new IFutureListener<IActionResult>() {
            @Override public void onHappened(final IActionResult result) {
                world.getController(ViewController.class).visualize(result).addListener(new IFutureListener<Void>() {
                    @Override public void onHappened(Void aVoid) {
                        result.apply(world);
                        apply(future, creature, world, idx + 1);
                    }
                });
            }
        });
    }


}
