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
import com.vlaaad.common.gdx.scene2d.ParticleActor;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.actions.results.imp.PoisonShotResult;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.view.IVisualizer;
import com.vlaaad.dice.game.world.view.ResultVisualizer;
import com.vlaaad.dice.managers.SoundManager;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

public class ParticlePoisonShotVisualizer implements IVisualizer<PoisonShotResult> {
    private static final Vector2 tmp = new Vector2();
    private final ResultVisualizer visualizer;

    public ParticlePoisonShotVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(PoisonShotResult result) {
        final Future<Void> future = Future.make();
        final ParticleActor actor = new ParticleActor("ability-" + result.ability.name);
        actor.setPosition(
            (result.creature.getX() + 0.5f) * ViewController.CELL_SIZE,
            (result.creature.getY() + 0.5f) * ViewController.CELL_SIZE + 3
        );
        SoundManager.instance.playFirstExistingMusicAsSound("ability-" + result.ability.name, "ability-poisonous-dust");
        Grid2D.Coordinate target = new Grid2D.Coordinate(result.target.getX(), result.target.getY());
        float time = tmp.set(result.creature.getX(), result.creature.getY()).sub(target.x(), target.y()).len() * 0.2f;
        actor.freeOnComplete();
        actor.addAction(sequence(
            moveTo(
                (target.x() + 0.5f) * ViewController.CELL_SIZE,
                (target.y() + 0.5f) * ViewController.CELL_SIZE,
                time
            ),
            run(new Runnable() {
                @Override public void run() {
                    actor.effect.allowCompletion();
                    future.happen();
                }
            })
        ));
        visualizer.viewController.effectLayer.addActor(actor);
        return future;
    }
}
