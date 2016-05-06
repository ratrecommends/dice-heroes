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

package com.vlaaad.dice.game.world.behaviours.processors.random;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.behaviours.RequestProcessor;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCreatureParams;
import com.vlaaad.dice.game.world.controllers.RandomController;
import com.vlaaad.dice.game.world.view.ViewScroller;

/**
 * Created 16.03.14 by vlaaad
 */
public class RandomCreatureProcessor extends RequestProcessor<Creature,AbilityCreatureParams> {
    @Override public int preProcess(AbilityCreatureParams params) {
        return 1;
    }

    @Override public IFuture<Creature> process(final AbilityCreatureParams params) {
        final Future<Creature> future = new Future<Creature>();
        params.creature.world.stage.addAction(Actions.delay(
            ViewScroller.CENTER_ON_TIME,
            Actions.run(new Runnable() {
                @Override public void run() {
                    future.happen(params.creature.world.getController(RandomController.class).random(params.available));
                }
            })
        ));
        return future;
    }
}
