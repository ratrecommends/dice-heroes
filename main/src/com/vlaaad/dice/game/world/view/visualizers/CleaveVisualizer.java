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
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.vlaaad.common.gdx.scene2d.events.AnimationListener;
import com.vlaaad.common.util.CountDown;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.imp.AttackResult;
import com.vlaaad.dice.game.actions.results.imp.CleaveResult;
import com.vlaaad.dice.game.actions.results.imp.GiveExpResult;
import com.vlaaad.dice.game.actions.results.imp.SequenceResult;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.view.*;
import com.vlaaad.dice.game.world.view.visualizers.objects.Death;
import com.vlaaad.dice.game.world.view.visualizers.objects.Defence;
import com.vlaaad.dice.managers.SoundManager;

/**
 * Created 24.11.13 by vlaaad
 */
public class CleaveVisualizer implements IVisualizer<CleaveResult> {

    private final ResultVisualizer visualizer;

    public CleaveVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(final CleaveResult result) {
        final Future<Void> future = new Future<Void>();
        final CountDown countDown = new CountDown(result.results.size + 1, new Runnable() {
            @Override public void run() {
                future.happen();
            }
        });
        final WorldObjectView actorView = visualizer.viewController.getView(result.creature);
        final AnimationSubView appear = new AnimationSubView(0.05f, Config.findRegions("animation/attack-" + result.attackType + "-" + result.attackLevel), Animation.PlayMode.NORMAL);
        visualizer.viewController.world.dispatcher.dispatch(ResultVisualizer.VISUALIZE_ATTACK, result.creature);
        actorView.addSubView(appear);
        //appear
        actorView.addListener(new AnimationListener() {
            @Override protected void onAnimationEvent(AnimationEvent event) {
                actorView.removeListener(this);
                actorView.removeSubView(appear);
                final ImageSubView sword = new ImageSubView("animation/attack-" + result.attackType + "-" + result.attackLevel + "-diagonal");
                actorView.addSubView(sword);
                //cleave
                sword.getActor().addAction(Actions.sequence(
                    Actions.moveBy(ViewController.CELL_SIZE / 2, ViewController.CELL_SIZE / 2, 0.1f),
                    Actions.run(new Runnable() {
                        @Override public void run() {
                            for (Creature target : result.targets) {
                                visualizer.viewController.visualize(new Defence(target, result.attackType));
                            }

                        }
                    }),
                    Actions.parallel(
                        Actions.rotateBy(360, 0.15f),
                        Actions.sequence(
                            Actions.delay(0.07f),
                            Actions.run(new Runnable() {
                                @Override public void run() {
                                    for (IActionResult sub : result.results) {
                                        SequenceResult sequence = (SequenceResult) sub;
                                        AttackResult attackResult = (AttackResult) sequence.results.get(0);
                                        String soundName = result.attackType.toString() + (attackResult.success ? "-kill" : "-miss");
                                        SoundManager.instance.playSoundIfExists(soundName);
                                        if (attackResult.success) {

                                            visualizer.viewController.visualize(new Death(attackResult.creature, attackResult.target)).addListener(countDown);
                                        } else {
                                            countDown.tick();
                                        }
                                    }
                                }
                            })
                        )
                    ),
                    Actions.run(new Runnable() {
                        @Override public void run() {
                            //disappear
                            actorView.removeSubView(sword);
                            Array<TextureAtlas.AtlasRegion> appearRegions = Config.findRegions("animation/attack-" + result.attackType + "-" + result.attackLevel);
                            Array<TextureAtlas.AtlasRegion> disappearRegions = new Array<TextureAtlas.AtlasRegion>(appearRegions);
                            disappearRegions.removeIndex(1);
                            disappearRegions.reverse();
                            final AnimationSubView disappear = new AnimationSubView(0.1f, disappearRegions, Animation.PlayMode.NORMAL);
                            actorView.addSubView(disappear);
                            actorView.addListener(new AnimationListener() {
                                @Override protected void onAnimationEvent(AnimationEvent event) {
                                    actorView.removeListener(this);
                                    actorView.removeSubView(disappear);
                                    ObjectIntMap<Creature> expResults = new ObjectIntMap<Creature>();
                                    for (IActionResult sub : result.results) {
                                        GiveExpResult expResult = (GiveExpResult) ((SequenceResult) sub).results.get(1);

                                        if (expResult.creature.player == expResult.creature.world.viewer) {
                                            expResults.getAndIncrement(expResult.creature, 0, expResult.exp);
                                        }
                                    }
                                    if (expResults.size == 0) {
                                        countDown.tick();
                                        return;
                                    }
                                    IActionResult[] expArray = new IActionResult[expResults.size];
                                    int i = 0;
                                    for (Creature creature : expResults.keys()) {
                                        expArray[i] = new GiveExpResult(creature, expResults.get(creature, 0));
                                        i++;
                                    }
                                    SequenceResult seq = new SequenceResult(expArray);
                                    visualizer.visualize(seq).addListener(new IFutureListener<Void>() {
                                        @Override public void onHappened(Void aVoid) {
                                            countDown.tick();
                                        }
                                    });
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
