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

package com.vlaaad.dice.game.world.behaviours.processors.ai;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.world.behaviours.RequestProcessor;
import com.vlaaad.dice.game.world.behaviours.params.TurnParams;
import com.vlaaad.dice.game.world.behaviours.responces.TurnResponse;
import com.vlaaad.dice.game.world.view.ViewScroller;

public class BossTurnProcessor extends RequestProcessor<TurnResponse, TurnParams> {
    @Override public int preProcess(TurnParams params) {
        return 1000;
    }

    @Override public IFuture<TurnResponse> process(final TurnParams params) {
        final Future<TurnResponse> future = new Future<TurnResponse>();
        params.creature.world.stage.addAction(Actions.delay(ViewScroller.CENTER_ON_TIME, Actions.run(new Runnable() {
            @Override public void run() {
                future.happen(new TurnResponse<Grid2D.Coordinate>(
                    TurnResponse.TurnAction.MOVE,
                    new Grid2D.Coordinate(params.creature.getX(), params.creature.getY())
                ));
            }
        })));
        return future;
    }
}
