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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.gdx.scene2d.ParticleActor;
import com.vlaaad.common.gdx.scene2d.events.AnimationListener;
import com.vlaaad.common.util.CountDown;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.AttackType;
import com.vlaaad.dice.game.actions.results.imp.ChainLightningResult;
import com.vlaaad.dice.game.actions.results.imp.GiveExpResult;
import com.vlaaad.dice.game.actions.results.imp.SequenceResult;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.view.AnimationSubView;
import com.vlaaad.dice.game.world.view.IVisualizer;
import com.vlaaad.dice.game.world.view.ResultVisualizer;
import com.vlaaad.dice.game.world.view.WorldObjectView;
import com.vlaaad.dice.game.world.view.visualizers.objects.Death;
import com.vlaaad.dice.game.world.view.visualizers.objects.Defence;
import com.vlaaad.dice.managers.SoundManager;
import com.vlaaad.dice.ui.components.SegmentActor;

/**
 * Created 12.01.14 by vlaaad
 */
public class ChainLightningVisualizer implements IVisualizer<ChainLightningResult> {

    private static final Vector2 tmp = new Vector2();

    private final ResultVisualizer visualizer;

    public ChainLightningVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(final ChainLightningResult result) {
        final Future<Void> future = new Future<Void>();
        final WorldObjectView casterView = visualizer.viewController.getView(result.caster);
//        Logger.log(Config.findRegions("animation/chain-lightning"));
        final AnimationSubView appear = new AnimationSubView(0.05f, Config.findRegions("animation/chain-lightning"), Animation.PlayMode.NORMAL);
        visualizer.viewController.scroller.centerOn(result.chain.first());
        casterView.addSubView(appear);
        casterView.addListener(new AnimationListener() {
            @Override protected void onAnimationEvent(AnimationEvent event) {
                casterView.removeListener(this);
                Array<Vector2> fullChain = new Array<Vector2>(result.chain.size + 1);
                fullChain.add(pointFor(result.caster, 19 / 24f, 23 / 24f));
                for (Creature target : result.chain) {
                    fullChain.add(pointFor(target));
                }

                Array<Vector2> lightningPoints = new Array<Vector2>();
                lightningPoints.add(fullChain.first());
                for (int i = 1; i < fullChain.size; i++) {
                    Vector2 from = fullChain.get(i - 1);
                    Vector2 to = fullChain.get(i);
                    int jogs = MathUtils.ceil(from.dst(to) * 1.3f / ViewController.CELL_SIZE);
                    //add (from, to]
                    if (jogs <= 0) {
                        lightningPoints.add(to);
                        continue;
                    }
                    Vector2 prev = from;
                    for (int j = 1; j <= jogs; j++) {
                        float alpha = (float) j / (jogs + 1f);
                        Vector2 step = new Vector2(from).lerp(to, alpha);
                        float stepLength = tmp.set(prev).dst(step);
                        Vector2 target = tmp.set(to).sub(from).rotate(MathUtils.randomBoolean() ? -90 : 90).nor().scl(MathUtils.random(2f, stepLength / 2f)).add(step);
                        step.set(target);
                        lightningPoints.add(step);
                        prev = step;
                    }
                    lightningPoints.add(to);
                }
                for (Creature creature : result.chain) {
                    final ParticleActor particles = new ParticleActor(Config.particles.get("ability-chain-lightning-hit").obtain());
                    particles.addListener(new ChangeListener() {
                        @Override public void changed(ChangeEvent event, Actor actor) {
                            particles.effect.free();
                            particles.remove();
                        }
                    });
                    particles.setPosition(
                        (creature.getX() + 0.5f) * ViewController.CELL_SIZE,
                        (creature.getY() + 0.5f) * ViewController.CELL_SIZE
                    );
                    visualizer.viewController.effectLayer.addActor(particles);

                    visualizer.viewController.visualize(new Defence(creature, AttackType.weapon));
                }
                final CountDown countDown = new CountDown(result.killed.size + 1, new Runnable() {
                    @Override public void run() {
                        future.happen();
                    }
                });
                for (Creature creature : result.killed) {
                    if (creature == result.caster && result.addedExp.containsKey(result.caster))
                        continue;

                    visualizer.viewController.visualize(new Death(result.caster, creature)).addListener(countDown);
                }
                SoundManager.instance.playSound("chain-lightning");
                for (int i = 1; i < lightningPoints.size; i++) {
                    Vector2 from = lightningPoints.get(i - 1);
                    Vector2 to = lightningPoints.get(i);
                    SegmentActor ray = new SegmentActor(tmp.set(to).sub(from), "effect-chain-lightning-segment");
                    ray.setPosition(from.x, from.y);
                    visualizer.viewController.effectLayer.addActor(ray);
                    ray.addAction(Actions.sequence(
                            Actions.delay(0.5f),
                            Actions.alpha(0, 0.5f),
                            Actions.removeActor())
                    );
                }
                visualizer.viewController.effectLayer.addAction(Actions.sequence(
                    Actions.delay(0.5f),
                    Actions.run(new Runnable() {
                        @Override public void run() {
                            casterView.removeSubView(appear);
                            Array<TextureAtlas.AtlasRegion> appearRegions = Config.findRegions("animation/chain-lightning");
                            Array<TextureAtlas.AtlasRegion> disappearRegions = new Array<TextureAtlas.AtlasRegion>(appearRegions);
                            disappearRegions.removeIndex(1);
                            disappearRegions.reverse();
                            final AnimationSubView disappear = new AnimationSubView(0.1f, disappearRegions, Animation.PlayMode.NORMAL);
                            casterView.addSubView(disappear);
                            casterView.addListener(new AnimationListener() {
                                @Override protected void onAnimationEvent(AnimationEvent event) {
                                    casterView.removeSubView(disappear);
                                    casterView.removeListener(this);
                                }
                            });
                        }
                    }),
                    Actions.delay(0.5f),
                    Actions.run(new Runnable() {
                        @Override public void run() {
                            if (result.addedExp.size == 0) {
                                countDown.tick();
                                return;
                            }
                            //exp
                            GiveExpResult[] expResults = new GiveExpResult[result.addedExp.size];
                            int i = 0;
                            for (Creature creature : result.addedExp.keys()) {
                                expResults[i] = new GiveExpResult(creature, result.addedExp.get(creature, 0));
                                i++;
                            }
                            visualizer.visualize(new SequenceResult(expResults)).addListener(new IFutureListener<Void>() {
                                @Override public void onHappened(Void aVoid) {
                                    countDown.tick();
                                    if (result.killed.contains(result.caster, true) && result.addedExp.containsKey(result.caster)) {

                                        visualizer.viewController.visualize(new Death(result.caster, result.caster)).addListener(countDown);
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

    private Vector2 pointFor(Creature creature) {
        return pointFor(creature, 0.5f, 0.5f);
    }

    private Vector2 pointFor(Creature creature, float tileOffsetX, float tileOffsetY) {
        return new Vector2(
            (creature.getX() + tileOffsetX) * ViewController.CELL_SIZE,
            (creature.getY() + tileOffsetY) * ViewController.CELL_SIZE
        );
    }
}
