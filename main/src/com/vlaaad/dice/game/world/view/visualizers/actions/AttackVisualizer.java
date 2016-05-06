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
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.gdx.scene2d.events.AnimationListener;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.results.imp.AttackResult;
import com.vlaaad.dice.game.world.view.*;
import com.vlaaad.dice.game.world.view.visualizers.objects.Death;
import com.vlaaad.dice.game.world.view.visualizers.objects.Defence;
import com.vlaaad.dice.managers.SoundManager;

/**
 * Created 23.11.13 by vlaaad
 */
public class AttackVisualizer implements IVisualizer<AttackResult> {

    private final ResultVisualizer visualizer;

    public AttackVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(final AttackResult result) {
        final Future<Void> future = new Future<Void>();
        visualizer.viewController.visualize(new Defence(result.target, result.type));
        String fallbackName = "animation/attack-" + result.type + "-" + result.attackLevel;
        String abilityBaseName = "animation/" + result.ability.name;
        boolean useAbilityName = Config.skin.getAtlas().findRegion(abilityBaseName) != null;
        final String baseName = useAbilityName ? abilityBaseName : fallbackName;

        // --------------------- actor attack --------------------- //

        final WorldObjectView actorView = visualizer.viewController.getView(result.creature);
        final AnimationSubView appear = new AnimationSubView(0.05f, Config.findRegions(baseName), Animation.PlayMode.NORMAL);
        actorView.addSubView(appear);
        visualizer.viewController.world.dispatcher.dispatch(ResultVisualizer.VISUALIZE_ATTACK, result.creature);
        //appear
        actorView.addListener(new AnimationListener() {
            @Override protected void onAnimationEvent(AnimationEvent event) {
                actorView.removeListener(this);
                actorView.removeSubView(appear);
                final int dx = result.target.getX() - result.creature.getX();
                final int dy = result.target.getY() - result.creature.getY();
                //rotate weapon
                final RotateODImagesSubView rotate = new RotateODImagesSubView(baseName, dx, dy, 0.05f, 0);
                actorView.addSubView(rotate);
                actorView.addListener(new AnimationListener() {
                    @Override protected void onAnimationEvent(AnimationEvent event) {
                        //move weapon

                        String soundName = result.type.toString() + (result.success ? "-kill" : "-miss");
                        if (SoundManager.instance.soundExists(soundName)) {
                            SoundManager.instance.playSound(soundName);
                        }

                        actorView.removeListener(this);
                        rotate.getActor().addAction(Actions.sequence(
                            Actions.moveBy(dx * 10, dy * 10, 0.1f),
                            Actions.run(new Runnable() {
                                @Override public void run() {
                                    if (result.success) {
                                        visualizer.viewController.visualize(new Death(result.creature, result.target)).addListener(future);
                                    }
                                }
                            }),
                            Actions.moveBy(-dx * 10, -dy * 10, 0.1f),
                            Actions.run(new Runnable() {
                                @Override public void run() {
                                    //rotate weapon back
                                    actorView.removeSubView(rotate);
                                    final RotateODImagesSubView rotateBack = new RotateODImagesSubView(baseName, 1, 1, 0.05f, rotate.getCurrentIndex());
                                    actorView.addSubView(rotateBack);
                                    actorView.addListener(new AnimationListener() {
                                        @Override protected void onAnimationEvent(AnimationEvent event) {
                                            actorView.removeSubView(rotateBack);
                                            actorView.removeListener(this);
                                            //disappear
                                            Array<TextureAtlas.AtlasRegion> appearRegions = Config.findRegions(baseName);
                                            Array<TextureAtlas.AtlasRegion> disappearRegions = new Array<TextureAtlas.AtlasRegion>(appearRegions);
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
                                    });
                                }
                            })
                        ));
                    }
                });
            }
        });
        return future;
    }

}
