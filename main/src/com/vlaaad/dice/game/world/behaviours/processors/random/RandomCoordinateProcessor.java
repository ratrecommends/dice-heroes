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
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.world.behaviours.RequestProcessor;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCoordinatesParams;
import com.vlaaad.dice.game.world.controllers.RandomController;
import com.vlaaad.dice.game.world.view.ViewScroller;

/**
 * Created 16.03.14 by vlaaad
 */
public class RandomCoordinateProcessor extends RequestProcessor<Grid2D.Coordinate,AbilityCoordinatesParams> {
    @Override public int preProcess(AbilityCoordinatesParams params) {
        return 1;
    }

    @Override public IFuture<Grid2D.Coordinate> process(final AbilityCoordinatesParams params) {
        final Future<Grid2D.Coordinate> future = new Future<Grid2D.Coordinate>();
        params.creature.world.stage.addAction(Actions.delay(
            ViewScroller.CENTER_ON_TIME,
            Actions.run(new Runnable() {
                @Override public void run() {
                    future.happen(params.creature.world.getController(RandomController.class).random(params.coordinates));
                }
            })
        ));
        return future;
    }
}
