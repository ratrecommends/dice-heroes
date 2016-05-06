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

import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.vlaaad.common.gdx.scene2d.ParticleActor;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.results.imp.AreaOfAttackResult;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.events.EffectEvent;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.events.EventListener;
import com.vlaaad.dice.game.world.events.EventType;
import com.vlaaad.dice.game.world.view.*;

/**
 * Created 26.01.14 by vlaaad
 */
public class AreaOfAttackVisualizer implements IVisualizer<AreaOfAttackResult> {

    private final ResultVisualizer visualizer;

    public AreaOfAttackVisualizer(ResultVisualizer visualizer) {

        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(final AreaOfAttackResult result) {

        final Future<Void> future = new Future<Void>();


        final EventListener<Creature> listener = new EventListener<Creature>() {
            @Override public void handle(EventType<Creature> type, Creature creature) {
                if (!result.targets.contains(creature, true))
                    return;
                final WorldObjectView view = visualizer.viewController.getView(creature);
                final ParticleEffectPool.PooledEffect particles = Config.particles.get("ability-" + result.ability.name).obtain();
                ParticleActor particleActor = new ParticleActor(particles);
                particleActor.setPosition(ViewController.CELL_SIZE / 2, ViewController.CELL_SIZE / 2);
                final ActorSubView subView = new ActorSubView(particleActor);
                particleActor.addListener(new ChangeListener() {
                    @Override public void changed(ChangeEvent event, Actor actor) {
                        particles.free();
                        view.removeSubView(subView);
                    }
                });
                view.addSubView(subView);
                particleActor.addAction(Actions.sequence(
                    Actions.delay(0.5f),
                    Actions.run(new Runnable() {
                        @Override public void run() {
                            particles.allowCompletion();
                        }
                    })
                ));
            }
        };
        visualizer.viewController.world.dispatcher.add(ResultVisualizer.VISUALIZE_ATTACK, listener);
        visualizer.viewController.world.dispatcher.add(Creature.REMOVE_EFFECT, new EventListener<EffectEvent>() {
            @Override public void handle(EventType<EffectEvent> type, EffectEvent event) {
                if (event.effect.eq(result.casterEffect)) {
                    visualizer.viewController.world.dispatcher.remove(Creature.REMOVE_EFFECT, this);
                    visualizer.viewController.world.dispatcher.remove(ResultVisualizer.VISUALIZE_ATTACK, listener);
                }
            }
        });

        final TileSubView image = new TileSubView("animation/levelup-white-cube", -1);
        final WorldObjectView casterView = visualizer.viewController.getView(result.creature);
        casterView.addSubView(image);
        casterView.addSubView(image);
        image.getActor().getColor().a = 0;
        image.getActor().addAction(Actions.sequence(
            Actions.alpha(1f, 0.4f),
            Actions.run(new Runnable() {
                @Override public void run() {
                    for (Creature target : result.targets) {
                        Tile tile = new Tile("ability/" + result.ability.name + "-get-icon");
                        tile.setPosition((target.getX() + 0.5f) * ViewController.CELL_SIZE - tile.getWidth() / 2,
                            (target.getY() + 1) * ViewController.CELL_SIZE);
                        visualizer.viewController.effectLayer.addActor(tile);
                        tile.addAction(Actions.sequence(
                            Actions.parallel(
                                Actions.moveBy(0, ViewController.CELL_SIZE / 2f, 1f),
                                Actions.alpha(0, 1f)
                            ),
                            Actions.removeActor()
                        ));
                    }

                    final ParticleEffectPool.PooledEffect effect = Config.particles.get("ability-" + result.ability.name + "-self").obtain();
                    final ParticleActor selfParticles = new ParticleActor(effect);
                    selfParticles.setPosition(
                        (result.creature.getX() + 0.5f) * ViewController.CELL_SIZE,
                        (result.creature.getY() + 0.5f) * ViewController.CELL_SIZE
                    );

                    visualizer.viewController.effectLayer.addActor(selfParticles);

                    selfParticles.addAction(Actions.sequence(
                        Actions.delay(0.5f),
                        Actions.run(new Runnable() {
                            @Override public void run() {
                                selfParticles.effect.allowCompletion();
                                future.happen();
                                selfParticles.addListener(new ChangeListener() {
                                    @Override public void changed(ChangeEvent event, Actor ignored) {
                                        effect.free();
                                        selfParticles.remove();
                                    }
                                });
                            }
                        })
                    ));
                }
            }),
            Actions.alpha(0f, 0.4f),
            Actions.run(new Runnable() {
                @Override public void run() {
                    casterView.removeSubView(image);
                }
            })
        ));
        return future;
    }
}
