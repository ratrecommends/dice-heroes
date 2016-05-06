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

import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.vlaaad.common.gdx.scene2d.ParticleActor;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.actions.results.imp.StupefactionResult;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.events.EffectEvent;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.events.EventListener;
import com.vlaaad.dice.game.world.events.EventType;
import com.vlaaad.dice.game.world.view.ActorSubView;
import com.vlaaad.dice.game.world.view.IVisualizer;
import com.vlaaad.dice.game.world.view.ResultVisualizer;
import com.vlaaad.dice.game.world.view.WorldObjectView;

/**
 * Created 23.03.14 by vlaaad
 */
public class StupefactionVisualizer implements IVisualizer<StupefactionResult> {
    private final ResultVisualizer visualizer;

    public StupefactionVisualizer(ResultVisualizer visualizer) {this.visualizer = visualizer;}

    @Override public IFuture<Void> visualize(final StupefactionResult result) {
        WorldObjectView view = visualizer.viewController.getView(result.target);
        final ParticleActor particles = new ParticleActor("ability-" + result.ability.name);
        particles.freeOnComplete();
        particles.setPosition(
            ViewController.CELL_SIZE * 0.5f,
            ViewController.CELL_SIZE * 0.5f + 3
        );
        view.addSubView(new ActorSubView(particles, -11));
        result.target.world.dispatcher.add(Creature.REMOVE_EFFECT, new EventListener<EffectEvent>() {
            @Override public void handle(EventType<EffectEvent> type, EffectEvent event) {
                if (event.creature != result.target || event.effect != result.effectToApply)
                    return;
                result.target.world.dispatcher.remove(Creature.REMOVE_EFFECT, this);
                particles.effect.allowCompletion();
            }
        });
        return Future.completed();
    }
}
