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
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.gdx.scene2d.events.AnimationListener;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.results.imp.TeleportResult;
import com.vlaaad.dice.game.actions.results.imp.TeleportTargetResult;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.view.AnimationSubView;
import com.vlaaad.dice.game.world.view.IVisualizer;
import com.vlaaad.dice.game.world.view.ResultVisualizer;
import com.vlaaad.dice.game.world.view.WorldObjectView;

/**
 * Created 01.06.14 by vlaaad
 */
public class TeleportTargetResultVisualizer implements IVisualizer<TeleportTargetResult> {
    private final ResultVisualizer visualizer;

    public TeleportTargetResultVisualizer(ResultVisualizer resultVisualizer) {
        visualizer = resultVisualizer;
    }

    @Override public IFuture<Void> visualize(final TeleportTargetResult result) {
        final Future<Void> future = Future.make();
        visualizer.viewController.scroller.centerOn(result.coordinate);
        final WorldObjectView view = visualizer.viewController.getView(result.caster);
        final AnimationSubView appear = new AnimationSubView(0.1f, Config.findRegions("animation/" + result.ability.name), Animation.PlayMode.NORMAL);
        appear.getActor().setPosition(
            ViewController.CELL_SIZE / 2 - appear.getActor().getWidth() / 2,
            ViewController.CELL_SIZE / 2 - appear.getActor().getHeight() / 2 + 2
        );
        view.addSubView(appear);
        appear.getActor().addListener(new AnimationListener() {
            @Override protected void onAnimationEvent(AnimationEvent event) {
                view.removeSubView(appear);
                Array<TextureAtlas.AtlasRegion> frames = new Array<TextureAtlas.AtlasRegion>(Config.findRegions("animation/" + result.ability.name));
                frames.removeIndex(1);
                frames.reverse();
                final AnimationSubView disappear = new AnimationSubView(0.1f, frames, Animation.PlayMode.NORMAL);
                disappear.getActor().setPosition(
                    ViewController.CELL_SIZE / 2 - appear.getActor().getWidth() / 2,
                    ViewController.CELL_SIZE / 2 - appear.getActor().getHeight() / 2 + 2
                );
                view.addSubView(disappear);
                disappear.getActor().addListener(new AnimationListener() {
                    @Override protected void onAnimationEvent(AnimationEvent event) {
                        view.removeSubView(disappear);
                        visualizer.getVisualizer(TeleportResult.class).visualize(result).addListener(future);
                    }
                });
            }
        });
        return future;
    }
}
