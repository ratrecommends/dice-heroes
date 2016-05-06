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

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.gdx.scene2d.events.AnimationListener;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.results.imp.TeleportResult;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.view.AnimationSubView;
import com.vlaaad.dice.game.world.view.IVisualizer;
import com.vlaaad.dice.game.world.view.ResultVisualizer;
import com.vlaaad.dice.game.world.view.WorldObjectView;
import com.vlaaad.dice.managers.SoundManager;

/**
 * Created 08.02.14 by vlaaad
 */
public class TeleportVisualizer implements IVisualizer<TeleportResult> {

    private static final float MOVE_OFFSET = ViewController.CELL_SIZE;

    private final ResultVisualizer visualizer;
    private final ObjectMap<String, Array<TextureAtlas.AtlasRegion>> composes = new ObjectMap<String, Array<TextureAtlas.AtlasRegion>>();

    public TeleportVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(final TeleportResult result) {
        final Future<Void> future = new Future<Void>();
        final Actor in = new AnimationSubView(0.125f, compose("animation/teleport-in"), Animation.PlayMode.NORMAL).getActor();
        in.setPosition(
            result.creature.getX() * ViewController.CELL_SIZE + (ViewController.CELL_SIZE - in.getWidth()) / 2,
            result.creature.getY() * ViewController.CELL_SIZE + ViewController.CELL_SIZE * 1.5f
        );
        visualizer.viewController.effectLayer.addActor(in);
        in.addListener(new AnimationListener() {
            @Override protected void onAnimationEvent(AnimationEvent event) {
                in.remove();
            }
        });
        WorldObjectView view = visualizer.viewController.getView(result.creature);
        visualizer.viewController.scroller.centerOn(result.creature);
        view.addAction(
            Actions.sequence(
                Actions.delay(0.625f),
                Actions.run(new Runnable() {
                    @Override public void run() {
                        SoundManager.instance.playSound("ability-teleport");
                    }
                }),
                Actions.parallel(
                    Actions.alpha(0, 0.5f, Interpolation.circleOut),
                    Actions.moveBy(0, MOVE_OFFSET, 0.5f)
                ),
                Actions.moveTo(result.coordinate.x() * ViewController.CELL_SIZE, result.coordinate.y() * ViewController.CELL_SIZE + MOVE_OFFSET),
                Actions.run(new Runnable() {
                    @Override public void run() {
                        final Actor out = new AnimationSubView(0.125f, compose("animation/teleport-out"), Animation.PlayMode.NORMAL).getActor();
                        out.setPosition(
                            result.coordinate.x() * ViewController.CELL_SIZE + (ViewController.CELL_SIZE - out.getWidth()) / 2,
                            result.coordinate.y() * ViewController.CELL_SIZE + ViewController.CELL_SIZE * 1.5f
                        );
                        visualizer.viewController.scroller.centerOn(result.coordinate);
                        visualizer.viewController.effectLayer.addActor(out);
                        out.addListener(new AnimationListener() {
                            @Override protected void onAnimationEvent(AnimationEvent event) {
                                out.remove();
                            }
                        });
                    }
                }),
                Actions.delay(0.625f),
                Actions.run(new Runnable() {
                    @Override public void run() {
                        SoundManager.instance.playSound("ability-teleport");
                    }
                }),
                Actions.parallel(
                    Actions.alpha(1f, 0.5f, Interpolation.circleIn),
                    Actions.moveBy(0, -MOVE_OFFSET + 2, 0.5f)
                ),
                Actions.run(new Runnable() {
                    @Override public void run() {
                        future.happen();
                    }
                })
            )
        );

        return future;
    }

    private Array<TextureAtlas.AtlasRegion> compose(String name) {
        Array<TextureAtlas.AtlasRegion> result = composes.get(name);
        if (result == null) {
            result = new Array<TextureAtlas.AtlasRegion>(Config.findRegions(name));
            Array<TextureAtlas.AtlasRegion> rev = new Array<TextureAtlas.AtlasRegion>(result);
            rev.pop();
            rev.reverse();
            for (int i = 0; i < 1; i++) {
                result.add(result.get(result.size - 2));
                result.add(result.get(result.size - 2));
            }
            result.addAll(rev);
            composes.put(name, result);
        }
        return result;
    }
}
