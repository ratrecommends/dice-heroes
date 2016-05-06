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
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.gdx.scene2d.events.AnimationListener;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.results.imp.SummonResult;
import com.vlaaad.dice.game.world.view.*;
import com.vlaaad.dice.managers.SoundManager;

/**
 * Created 09.02.14 by vlaaad
 */
public class SummonVisualizer implements IVisualizer<SummonResult> {
    private final ResultVisualizer visualizer;

    public SummonVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(final SummonResult result) {
        final Future<Void> future = new Future<Void>();
        Array<TextureAtlas.AtlasRegion> frames = Config.findRegions("animation/summon-" + result.summoned.player.getPlayerRelation(visualizer.viewController.world.viewer));
        final AnimationSubView animation = new AnimationSubView(0.1f, frames, Animation.PlayMode.NORMAL);
        result.summoned.setPosition(result.coordinate.x(), result.coordinate.y());
        final WorldObjectView view = visualizer.viewController.addView(result.summoned);
        for (SubView subView : view.subViews()) {
            subView.getActor().getColor().a = 0;
        }
        SoundManager.instance.playSound("ability-summon");
        view.addSubView(animation);
        view.addListener(new AnimationListener() {
            @Override protected void onAnimationEvent(AnimationEvent event) {
                view.removeListener(this);
                view.removeSubView(animation);
                for (SubView subView : view.subViews()) {
                    subView.getActor().getColor().a = 1f;
                }
                view.addAction(Actions.sequence(
                    Actions.moveBy(0, 6, 0.2f, Interpolation.swingOut),
                    Actions.moveBy(0, -6, 0.2f, Interpolation.elastic),
                    Actions.run(new Runnable() {
                        @Override public void run() {
                            visualizer.viewController.removeView(result.summoned);
                            future.happen();
                        }
                    })
                ));
            }
        });

        return future;
    }
}
