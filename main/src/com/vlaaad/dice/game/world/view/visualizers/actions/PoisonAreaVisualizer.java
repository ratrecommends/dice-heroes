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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.vlaaad.common.gdx.scene2d.ParticleActor;
import com.vlaaad.common.util.CountDown;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.actions.results.imp.PoisonAreaResult;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.view.IVisualizer;
import com.vlaaad.dice.game.world.view.ResultVisualizer;
import com.vlaaad.dice.managers.SoundManager;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

/**
 * Created 14.05.14 by vlaaad
 */
public class PoisonAreaVisualizer implements IVisualizer<PoisonAreaResult> {
    private static final Vector2 tmp = new Vector2();
    private final ResultVisualizer visualizer;

    public PoisonAreaVisualizer(ResultVisualizer visualizer) {this.visualizer = visualizer;}

    @Override public IFuture<Void> visualize(final PoisonAreaResult result) {
        final Future<Void> future = Future.make();
        final ParticleActor actor = new ParticleActor("ability-" + result.ability.name);
        actor.setPosition(
            (result.creature.getX() + 0.5f) * ViewController.CELL_SIZE,
            (result.creature.getY() + 0.5f) * ViewController.CELL_SIZE + 3
        );
        SoundManager.instance.playMusicAsSound("ability-" + result.ability.name);
        float time = tmp.set(result.creature.getX(), result.creature.getY()).sub(result.coordinate.x(), result.coordinate.y()).len() * 0.2f;
        actor.freeOnComplete();
        actor.addAction(sequence(
            moveTo(
                (result.coordinate.x() + 0.5f) * ViewController.CELL_SIZE,
                (result.coordinate.y() + 0.5f) * ViewController.CELL_SIZE,
                time
            ),
            run(new Runnable() {
                @Override public void run() {
                    actor.effect.allowCompletion();
                    final CountDown countDown = new CountDown(result.targets.size, future);
                    for (Creature creature : result.targets) {
                        ParticleActor hit = new ParticleActor("ability-" + result.ability.name + "-hit");
                        visualizer.viewController.effectLayer.addActor(hit);
                        hit.setPosition(
                            (creature.getX() + 0.5f) * ViewController.CELL_SIZE,
                            (creature.getY() + 0.5f) * ViewController.CELL_SIZE + 3
                        );
                        hit.freeOnComplete();
                        hit.addListener(new ChangeListener() {
                            @Override public void changed(ChangeEvent event, Actor actor) {
                                countDown.tick();
                            }
                        });
                    }
                }
            })
        ));
        visualizer.viewController.effectLayer.addActor(actor);
        return future;
    }
}
