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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.results.IAbilityOwner;
import com.vlaaad.dice.game.actions.results.IActorResult;
import com.vlaaad.dice.game.actions.results.ITargetOwner;
import com.vlaaad.dice.game.world.view.IVisualizer;
import com.vlaaad.dice.game.world.view.ResultVisualizer;
import com.vlaaad.dice.game.world.view.WorldObjectView;
import com.vlaaad.dice.managers.SoundManager;

public class CommonShotVisualizer<T extends IAbilityOwner & ITargetOwner & IActorResult> implements IVisualizer<T> {
    private static final Vector2 tmp = new Vector2();
    private final ResultVisualizer visualizer;

    private CommonShotVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    public static <T extends IAbilityOwner & ITargetOwner & IActorResult> CommonShotVisualizer<T> make(ResultVisualizer visualizer) {
        return new CommonShotVisualizer<T>(visualizer);
    }

    @Override public IFuture<Void> visualize(final T result) {
        final Future<Void> future = Future.make();
        final WorldObjectView actorView = visualizer.viewController.getView(result.getActor());
        WorldObjectView targetView = visualizer.viewController.getView(result.getTarget());
        visualizer.viewController.world.dispatcher.dispatch(ResultVisualizer.VISUALIZE_ATTACK, result.getActor());
        Vector2 direction = tmp
            .set(result.getTarget().getX(), result.getTarget().getY())
            .sub(result.getActor().getX(), result.getActor().getY());

        float dx = targetView.getX() - actorView.getX();
        float dy = targetView.getY() - actorView.getY();
        visualizer.viewController.scroller.centerOn(result.getTarget());

        final Image arrow = new Image(Config.skin,"animation/" + result.getAbility().name + "-shot");
        arrow.setPosition(actorView.getX(), actorView.getY());
        visualizer.viewController.effectLayer.addActor(arrow);
        arrow.setOrigin(13, 14);
        arrow.setRotation(direction.angle() - 45);
        arrow.addAction(Actions.sequence(
            Actions.moveBy(dx, dy, tmp.set(dx, dy).len() * 0.002f),
            Actions.run(new Runnable() {
                @Override public void run() {
                    SoundManager.instance.playSoundIfExists(result.getAbility().soundName);
                    arrow.remove();
                    future.happen();
                }
            })
        ));
        return future;
    }
}
