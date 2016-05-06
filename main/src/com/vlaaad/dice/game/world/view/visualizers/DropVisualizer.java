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

package com.vlaaad.dice.game.world.view.visualizers;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.view.IVisualizer;
import com.vlaaad.dice.game.world.view.ResultVisualizer;
import com.vlaaad.dice.game.world.view.Tile;
import com.vlaaad.dice.game.world.view.visualizers.objects.DroppedItem;

/**
 * Created 06.03.14 by vlaaad
 */
public class DropVisualizer implements IVisualizer<DroppedItem> {
    private final ResultVisualizer visualizer;

    public DropVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(DroppedItem drop) {
        final Future<Void> future = new Future<Void>();

        Group group = new Group();
        Tile image = new Tile("item/" + drop.item.name);
        Label counter = new Label(String.valueOf(drop.count), Config.skin);
        counter.setSize(image.getWidth(), image.getHeight());
        counter.setAlignment(Align.right | Align.bottom);
        group.addActor(image);
        group.addActor(counter);
        group.setTransform(false);
        visualizer.viewController.notificationLayer.addActor(group);
        group.setPosition(drop.target.getX() * ViewController.CELL_SIZE, drop.target.getY() * ViewController.CELL_SIZE);
        group.addAction(Actions.parallel(
            Actions.moveBy(0, 30, 1f, Interpolation.fade),
            Actions.alpha(0, 1f, Interpolation.fade),
            Actions.delay(0.4f, Actions.run(new Runnable() {
                @Override public void run() {
                    future.happen();
                }
            }))
        ));
        return future;
    }
}
