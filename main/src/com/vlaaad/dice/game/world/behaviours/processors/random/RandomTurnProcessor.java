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

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.world.behaviours.RequestProcessor;
import com.vlaaad.dice.game.world.behaviours.params.TurnParams;
import com.vlaaad.dice.game.world.behaviours.responces.TurnResponse;
import com.vlaaad.dice.game.world.controllers.RandomController;
import com.vlaaad.dice.game.world.view.ViewScroller;

/**
 * Created 16.03.14 by vlaaad
 */
public class RandomTurnProcessor extends RequestProcessor<TurnResponse, TurnParams> {
    @Override public int preProcess(TurnParams params) {
        return 1;
    }

    @Override public IFuture<TurnResponse> process(final TurnParams params) {
        final Future<TurnResponse> future = new Future<TurnResponse>();
        params.creature.world.stage.addAction(Actions.delay(
            ViewScroller.CENTER_ON_TIME,
            Actions.run(new Runnable() {
                @Override public void run() {
                    Array<Ability> profAbilities = params.availableProfessionAbilities;
                    RandomController r = params.creature.world.getController(RandomController.class);
                    boolean move = profAbilities.size == 0 || r.randomBoolean();
                    if (move) {
                        future.happen(new TurnResponse<Grid2D.Coordinate>(TurnResponse.TurnAction.MOVE, r.random(params.availableCells)));
                    } else {
                        future.happen(new TurnResponse<Ability>(TurnResponse.TurnAction.PROFESSION_ABILITY, r.random(params.availableProfessionAbilities)));
                    }
                }
            })
        ));
        return future;
    }
}
