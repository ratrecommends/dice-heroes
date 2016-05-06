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
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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
import com.vlaaad.dice.game.actions.results.imp.FirestormResult;
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

/**
 * Created 17.01.14 by vlaaad
 */
public class FirestormVisualizer implements IVisualizer<FirestormResult> {
    private static final Vector2 tmp = new Vector2();

    private final ResultVisualizer visualizer;

    public FirestormVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(final FirestormResult result) {
        final Future<Void> future = new Future<Void>();
        ParticleEffectPool.PooledEffect effect = Config.particles.get("ability-" + result.ability.name).obtain();
        String animationName = "animation/" + result.ability.name;
        final Array<TextureAtlas.AtlasRegion> appearRegions = Config.findRegions(animationName);
        final AnimationSubView appear = new AnimationSubView(0.05f, appearRegions, Animation.PlayMode.NORMAL);
        final WorldObjectView casterView = visualizer.viewController.getView(result.caster);
        casterView.addSubView(appear);
        casterView.addListener(new AnimationListener() {
            @Override protected void onAnimationEvent(AnimationEvent event) {
                casterView.removeListener(this);
                casterView.addAction(Actions.sequence(
                    Actions.delay(0.5f),
                    Actions.run(new Runnable() {
                        @Override public void run() {
                            casterView.removeSubView(appear);
                            Array<TextureAtlas.AtlasRegion> disappearRegions = new Array<TextureAtlas.AtlasRegion>(appearRegions);
                            disappearRegions.removeIndex(1);
                            disappearRegions.reverse();
                            final AnimationSubView disappear = new AnimationSubView(0.05f, disappearRegions, Animation.PlayMode.NORMAL);
                            casterView.addSubView(disappear);
                            casterView.addListener(new AnimationListener() {
                                @Override protected void onAnimationEvent(AnimationEvent event) {
                                    casterView.removeListener(this);
                                    casterView.removeSubView(disappear);
                                }
                            });
                        }
                    })
                ));
            }
        });
        final ParticleActor particleActor = new ParticleActor(effect);
        particleActor.getColor().a = 0;
        final Vector2 position = tmp.set(result.cell.x() + 0.5f, result.cell.y() + 0.5f).scl(ViewController.CELL_SIZE);
        particleActor.setPosition(position.x, position.y + 160);
        visualizer.viewController.effectLayer.addActor(particleActor);
        SoundManager.instance.playSound("ability-fireball");
        visualizer.viewController.scroller.centerOn(result.cell.x(), result.cell.y());
        particleActor.addAction(Actions.alpha(1f, 0.5f));
        particleActor.addAction(Actions.sequence(
            Actions.moveTo(position.x, position.y, 1f),
            Actions.run(new Runnable() {
                @Override public void run() {
                    particleActor.addListener(new ChangeListener() {
                        @Override public void changed(ChangeEvent event, Actor actor) {
                            particleActor.remove();
                            particleActor.effect.free();
                        }
                    });
                    particleActor.effect.allowCompletion();
                    for (Creature creature : result.underAttack) {
                        visualizer.viewController.visualize(new Defence(creature, AttackType.weapon));
                    }
                    //tick for every death + 1 tick for exp
                    final CountDown countDown = new CountDown(result.killed.size + 1, new Runnable() {
                        @Override public void run() {
                            future.happen();
                        }
                    });
                    for (Creature creature : result.killed) {
                        //if caster gains exp, give him exp, then kill
                        if (creature == result.caster && result.addedExp.containsKey(result.caster))
                            continue;

                        visualizer.viewController.visualize(new Death(result.caster, creature)).addListener(countDown);
                    }
                    SoundManager.instance.playSound("ability-firestorm");
                    final ParticleEffectPool.PooledEffect hitEffect = Config.particles.get("ability-" + result.ability.name + "-hit").obtain();
                    final ParticleActor hitActor = new ParticleActor(hitEffect);
                    visualizer.viewController.spawnLayer.addActor(hitActor);
                    hitActor.setPosition(position.x, position.y);
                    hitActor.addListener(new ChangeListener() {
                        @Override public void changed(ChangeEvent event, Actor actor) {
                            hitActor.remove();
                            hitEffect.free();
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
                    });
                }
            })
        ));
        return future;
    }
}
