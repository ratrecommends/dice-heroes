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

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.gdx.scene2d.events.AnimationListener;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.results.imp.AttackResult;
import com.vlaaad.dice.game.world.view.AnimationSubView;
import com.vlaaad.dice.game.world.view.IVisualizer;
import com.vlaaad.dice.game.world.view.ResultVisualizer;
import com.vlaaad.dice.game.world.view.WorldObjectView;
import com.vlaaad.dice.game.world.view.visualizers.objects.Death;
import com.vlaaad.dice.managers.SoundManager;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public class DoubleDefensiveAttackVisualizer implements IVisualizer<AttackResult> {
    public static final float ANIMATION_TIME = 0.15f;
    private final ResultVisualizer visualizer;

    public DoubleDefensiveAttackVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(final AttackResult result) {
        final Future<Void> future = Future.make();
        final WorldObjectView actorView = visualizer.viewController.getView(result.creature);
        final Array<AtlasRegion> regions = Config.findRegions("animation/" + result.ability.name);
        final AnimationSubView appear = new AnimationSubView(0.05f, regions, Animation.PlayMode.NORMAL);
        actorView.addSubView(appear);
        visualizer.viewController.world.dispatcher.dispatch(ResultVisualizer.VISUALIZE_ATTACK, result.creature);
        actorView.addListener(new AnimationListener() {
            @Override protected void onAnimationEvent(AnimationEvent event) {
                actorView.removeListener(this);
                actorView.removeSubView(appear);
                final Image claw = new Image(regions.peek());
                actorView.addActor(claw);
                claw.setOrigin(Align.center);

                final int dx = result.target.getX() - result.creature.getX();
                final int dy = result.target.getY() - result.creature.getY();

                claw.addAction(rotateBy(360, ANIMATION_TIME));
                claw.addAction(sequence(
                    moveBy(dx * 10, dy * 10, ANIMATION_TIME * 0.5f),
                    run(new Runnable() {
                        @Override public void run() {
                            String soundName = result.type.toString() + (result.success ? "-kill" : "-miss");
                            if (SoundManager.instance.soundExists(soundName)) {
                                SoundManager.instance.playSound(soundName);
                            }
                            if (result.success) {

                                visualizer.viewController.visualize(new Death(result.creature, result.target)).addListener(future);
                            }
                        }
                    }),
                    moveBy(-dx * 10, -dy * 10, ANIMATION_TIME * 0.5f),
                    run(new Runnable() {
                        @Override public void run() {
                            claw.remove();
                            Array<TextureAtlas.AtlasRegion> disappearRegions = new Array<TextureAtlas.AtlasRegion>(regions);
                            disappearRegions.removeIndex(1);
                            disappearRegions.reverse();
                            final AnimationSubView disappear = new AnimationSubView(0.1f, disappearRegions, Animation.PlayMode.NORMAL);
                            actorView.addSubView(disappear);
                            actorView.addListener(new AnimationListener() {
                                @Override protected void onAnimationEvent(AnimationEvent event) {
                                    actorView.removeSubView(disappear);
                                    actorView.removeListener(this);
                                    if (!result.success) {
                                        future.happen();
                                    }
                                }
                            });
                        }
                    })
                ));
            }
        });
        return future;
    }
}
