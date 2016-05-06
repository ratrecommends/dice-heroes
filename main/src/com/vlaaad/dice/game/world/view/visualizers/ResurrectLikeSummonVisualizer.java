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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.vlaaad.common.gdx.scene2d.ParticleActor;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.results.imp.SummonResult;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.view.IVisualizer;
import com.vlaaad.dice.game.world.view.ResultVisualizer;
import com.vlaaad.dice.game.world.view.WorldObjectView;
import com.vlaaad.dice.managers.SoundManager;

import static com.vlaaad.dice.game.world.view.visualizers.actions.ResurrectVisualizer.PARTICLE_OFFSET;

public class ResurrectLikeSummonVisualizer implements IVisualizer<SummonResult> {

    private final ResultVisualizer visualizer;

    public ResurrectLikeSummonVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(final SummonResult result) {
        final Future<Void> future = new Future<Void>();
        final ParticleActor particle = new ParticleActor(Config.particles.get("ability-summon-" + result.summoned.profession.name).obtain());
        particle.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                particle.remove();
                particle.effect.free();
            }
        });
        particle.setPosition(
            ViewController.CELL_SIZE * result.coordinate.x() + ViewController.CELL_SIZE / 2f,
            ViewController.CELL_SIZE * result.coordinate.y() + PARTICLE_OFFSET
        );
        particle.addAction(Actions.moveBy(0, -PARTICLE_OFFSET + 5, 1f));
        visualizer.viewController.effectLayer.addActor(particle);
        result.summoned.setPosition(result.coordinate.x(), result.coordinate.y());
        WorldObjectView view = visualizer.viewController.addView(result.summoned);
        view.getColor().a = 0;
        SoundManager.instance.playMusicAsSound("ability-boss-summon");
        view.addAction(Actions.sequence(
            Actions.alpha(1f, 1f),
            Actions.run(new Runnable() {
                @Override public void run() {
                    particle.effect.allowCompletion();
                    visualizer.viewController.removeView(result.summoned);
                    future.happen();
                }
            })
        ));
        return future;
    }
}
