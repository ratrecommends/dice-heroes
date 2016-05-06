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

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.vlaaad.common.gdx.scene2d.ParticleActor;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.actions.results.imp.EnthrallmentResult;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.view.IVisualizer;
import com.vlaaad.dice.game.world.view.ResultVisualizer;
import com.vlaaad.dice.game.world.view.WorldObjectView;
import com.vlaaad.dice.managers.SoundManager;

/**
 * Created 18.06.14 by vlaaad
 */
public class EnthrallmentVisualizer implements IVisualizer<EnthrallmentResult> {
    private static final Vector2 tmp = new Vector2();

    private final ResultVisualizer visualizer;

    public EnthrallmentVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(final EnthrallmentResult result) {
        final Future<Void> future = Future.make();
        SoundManager.instance.playFirstExistingMusicAsSound("ability-" + result.ability.name, "ability-enthrallment");
        final ParticleActor particles = new ParticleActor("ability-" + result.ability.name);
        visualizer.viewController.scroller.centerOn(result.target);
        visualizer.viewController.effectLayer.addActor(particles);
        particles.freeOnComplete();
        particles.setPosition(
            (result.creature.getX() + 0.5f) * ViewController.CELL_SIZE,
            (result.creature.getY() + 0.5f) * ViewController.CELL_SIZE
        );
        particles.addAction(Actions.sequence(
            Actions.moveTo(
                (result.target.getX() + 0.5f) * ViewController.CELL_SIZE,
                (result.target.getY() + 0.5f) * ViewController.CELL_SIZE,
                tmp.set(result.creature.getX() - result.target.getX(), result.creature.getY() - result.target.getY()).len() * 0.15f
            ),
            Actions.run(new Runnable() {
                @Override public void run() {
                    particles.effect.allowCompletion();
                    WorldObjectView toView = visualizer.viewController.getView(result.target);
                    toView.addAction(Actions.sequence(
                        Actions.moveBy(0, 5, 0.5f),
                        Actions.run(future),
                        Actions.moveBy(0, -5, 0.2f, Interpolation.exp10Out)
                    ));
                }
            })
        ));
        return future;
    }
}
