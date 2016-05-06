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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.actions.results.imp.ShotResult;
import com.vlaaad.dice.game.world.view.IVisualizer;
import com.vlaaad.dice.game.world.view.ImageSubView;
import com.vlaaad.dice.game.world.view.ResultVisualizer;
import com.vlaaad.dice.game.world.view.WorldObjectView;
import com.vlaaad.dice.game.world.view.visualizers.objects.Death;
import com.vlaaad.dice.game.world.view.visualizers.objects.Defence;
import com.vlaaad.dice.managers.SoundManager;

/**
 * Created 24.11.13 by vlaaad
 */
public class ShotVisualizer implements IVisualizer<ShotResult> {

    private static final Vector2 tmp = new Vector2();

    private final ResultVisualizer visualizer;

    public ShotVisualizer(ResultVisualizer visualizer) {this.visualizer = visualizer;}

    @Override public IFuture<Void> visualize(final ShotResult result) {
        final Future<Void> future = new Future<Void>();
        final WorldObjectView actorView = visualizer.viewController.getView(result.creature);
        WorldObjectView targetView = visualizer.viewController.getView(result.target);
        visualizer.viewController.world.dispatcher.dispatch(ResultVisualizer.VISUALIZE_ATTACK, result.creature);
        Vector2 direction = tmp.set(result.target.getX(), result.target.getY()).sub(result.creature.getX(), result.creature.getY());

        float dx = targetView.getX() - actorView.getX();
        float dy = targetView.getY() - actorView.getY();
        visualizer.viewController.scroller.centerOn(result.target);

        final ImageSubView arrow = new ImageSubView("animation/arrow-" + result.attackLevel);
        arrow.getActor().setOrigin(13, 14);
        arrow.getActor().setRotation(direction.angle() - 45);
        visualizer.viewController.visualize(new Defence(result.target, result.type));
        arrow.getActor().addAction(Actions.sequence(
            Actions.moveBy(dx, dy, tmp.set(dx, dy).len() * 0.002f),
            Actions.run(new Runnable() {
                @Override public void run() {
                    SoundManager.instance.playSound("arrow-shot");
                    actorView.removeSubView(arrow);
                    if (result.success) {

                        visualizer.viewController.visualize(new Death(result.creature, result.target)).addListener(future);
                    } else {
                        future.happen();
                    }
                }
            })
        ));
        actorView.addSubView(arrow);
        return future;
    }
}
