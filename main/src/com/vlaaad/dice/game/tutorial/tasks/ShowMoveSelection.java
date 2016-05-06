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

package com.vlaaad.dice.game.tutorial.tasks;

import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.tutorial.TutorialTask;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.controllers.RoundController;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.states.PvePlayState;

/**
 * Created 17.11.13 by vlaaad
 */
public class ShowMoveSelection extends TutorialTask {

    private final String selectionKey;

    public ShowMoveSelection(String selectionKey) {
        this.selectionKey = selectionKey;
    }

    @Override public void start(Callback callback) {
        PvePlayState playState = resources.get("playState");
        World world = playState.world;
        Creature current = world.getController(RoundController.class).getCurrentCreature();
        int x = current.getX();
        int y = current.getY();
        Array<Grid2D.Coordinate> coordinates = new Array<Grid2D.Coordinate>();
        addCoordinate(current, coordinates, world, x - 1, y - 1);
        addCoordinate(current, coordinates, world, x - 1, y);
        addCoordinate(current, coordinates, world, x - 1, y + 1);

        addCoordinate(current, coordinates, world, x, y - 1);
        addCoordinate(current, coordinates, world, x, y);
        addCoordinate(current, coordinates, world, x, y + 1);

        addCoordinate(current, coordinates, world, x + 1, y - 1);
        addCoordinate(current, coordinates, world, x + 1, y);
        addCoordinate(current, coordinates, world, x + 1, y + 1);

        ViewController viewController = world.getController(ViewController.class);
        if (viewController.hasSelection(selectionKey)) {
            viewController.removeSelection(selectionKey);
        }
        viewController.showSelection(selectionKey, coordinates, "selection/move");
        callback.taskEnded();
    }

    private void addCoordinate(Creature current, Array<Grid2D.Coordinate> coordinates, World world, int x, int y) {
        if (current.getX() == x && current.getY() == y) {
            coordinates.add(new Grid2D.Coordinate(x, y));
        } else if (world.canStepTo(x, y)) {
            coordinates.add(new Grid2D.Coordinate(x, y));
        }
    }
}
