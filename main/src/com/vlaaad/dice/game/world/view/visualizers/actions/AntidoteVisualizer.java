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

package com.vlaaad.dice.game.world.view.visualizers.actions;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.actions.results.imp.AntidoteResult;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.view.*;

/**
 * Created 10.02.14 by vlaaad
 */
public class AntidoteVisualizer implements IVisualizer<AntidoteResult> {
    private final ResultVisualizer visualizer;

    public AntidoteVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(final AntidoteResult result) {
        final Future<Void> future = new Future<Void>();
        final TileSubView image = new TileSubView("animation/levelup-white-cube", -1);
        final WorldObjectView view = visualizer.viewController.getView(result.target);
        view.addSubView(image);
        image.getActor().getColor().a = 0f;
        image.getActor().addAction(Actions.sequence(
            Actions.alpha(1f, 0.5f),
            Actions.run(new Runnable() {
                @Override public void run() {
                    Tile tile = new Tile("game-antidote-get-icon");
                    tile.setPosition((result.target.getX() + 0.5f) * ViewController.CELL_SIZE - tile.getWidth() / 2,
                        (result.target.getY() + 1) * ViewController.CELL_SIZE);
                    visualizer.viewController.effectLayer.addActor(tile);
                    tile.addAction(Actions.sequence(
                        Actions.parallel(
                            Actions.moveBy(0, ViewController.CELL_SIZE / 2f, 1f),
                            Actions.alpha(0, 1f)
                        ),
                        Actions.removeActor()
                    ));
                }
            }),
            Actions.alpha(0, 0.5f),
            Actions.run(new Runnable() {
                @Override public void run() {
                    view.removeSubView(image);
                    future.happen();
                }
            })
        ));
        return future;
    }
}
