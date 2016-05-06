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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.vlaaad.common.gdx.scene2d.ParticleActor;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.results.imp.FreezeResult;
import com.vlaaad.dice.game.effects.FreezeEffect;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.events.EffectEvent;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.events.EventListener;
import com.vlaaad.dice.game.world.events.EventType;
import com.vlaaad.dice.game.world.view.IVisualizer;
import com.vlaaad.dice.game.world.view.ResultVisualizer;
import com.vlaaad.dice.game.world.view.TileSubView;
import com.vlaaad.dice.game.world.view.WorldObjectView;
import com.vlaaad.dice.managers.SoundManager;

/**
 * Created 10.01.14 by vlaaad
 */
public class FreezeResultVisualizer implements IVisualizer<FreezeResult> {

    private static final Vector2 tmp = new Vector2();

    private final ResultVisualizer visualizer;

    public FreezeResultVisualizer(ResultVisualizer visualizer) {this.visualizer = visualizer;}

    @Override public IFuture<Void> visualize(final FreezeResult result) {
        final Future<Void> future = new Future<Void>();
        final Group layer = visualizer.viewController.effectLayer;
        final ParticleEffectPool.PooledEffect effect = Config.particles.get("ability-" + result.ability.name).obtain();
        ParticleActor actor = new ParticleActor(effect);
        actor.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                effect.free();
                actor.remove();
            }
        });
        layer.addActor(actor);
        actor.setPosition(
            ViewController.CELL_SIZE * (result.creature.getX() + 0.5f),
            ViewController.CELL_SIZE * (result.creature.getY() + 0.5f)
        );
        float time = tmp.set(result.creature.getX() - result.target.getX(), result.creature.getY() - result.target.getY()).len() * 0.1f;
        actor.addAction(Actions.sequence(
            Actions.moveTo(ViewController.CELL_SIZE * (result.target.getX() + 0.5f), ViewController.CELL_SIZE * (result.target.getY() + 0.5f), time),
            Actions.run(new Runnable() {
                @Override public void run() {
                    effect.allowCompletion();
                    final TileSubView freeze = new TileSubView("creature-effect-freeze");
                    freeze.getActor().getColor().a = 0f;
                    SoundManager.instance.playSound("ability-freeze-hit");
                    final WorldObjectView view = visualizer.viewController.getView(result.target);
                    view.addSubView(freeze);
                    freeze.getActor().addAction(Actions.sequence(
                        Actions.alpha(1, 0.5f),
                        Actions.run(new Runnable() {
                            @Override public void run() {
                                future.happen();
                            }
                        })
                    ));
                    visualizer.viewController.world.dispatcher.add(Creature.REMOVE_EFFECT, new EventListener<EffectEvent>() {
                        @Override public void handle(EventType<EffectEvent> type, EffectEvent event) {
                            if (event.creature == result.target && event.effect instanceof FreezeEffect) {
                                freeze.getActor().addAction(Actions.sequence(
                                    Actions.alpha(0, 0.5f),
                                    Actions.run(new Runnable() {
                                        @Override public void run() {
                                            view.removeSubView(freeze);
                                        }
                                    })
                                ));
                            }
                        }
                    });

                }
            })));
        return future;
    }

}
