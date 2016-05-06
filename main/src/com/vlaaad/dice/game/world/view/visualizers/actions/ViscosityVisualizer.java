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

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.actions.results.imp.ViscosityResult;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.events.EffectEvent;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.events.EventListener;
import com.vlaaad.dice.game.world.events.EventType;
import com.vlaaad.dice.game.world.view.IVisualizer;
import com.vlaaad.dice.game.world.view.ResultVisualizer;
import com.vlaaad.dice.game.world.view.Tile;
import com.vlaaad.dice.managers.SoundManager;

/**
 * Created 22.03.14 by vlaaad
 */
public class ViscosityVisualizer implements IVisualizer<ViscosityResult> {

    private final ResultVisualizer visualizer;

    public ViscosityVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(final ViscosityResult result) {
        final Future<Void> future = new Future<Void>();
        final Tile tile = new Tile("effect-" + result.ability.name + "-image");
        visualizer.viewController.coverLayer.addActor(tile);
        tile.setPosition(
            result.target.getX() * ViewController.CELL_SIZE + (ViewController.CELL_SIZE - tile.getWidth()) * 0.5f,
            result.target.getY() * ViewController.CELL_SIZE + (ViewController.CELL_SIZE - tile.getHeight()) * 0.5f
        );
        SoundManager.instance.playFirstExistingSound("ability-" + result.ability.name, "ability-potion-of-viscosity");
        tile.getColor().a = 0;
        tile.addAction(Actions.sequence(
            Actions.alpha(1, 0.25f),
            Actions.run(future)
        ));
        result.target.world.dispatcher.add(Creature.REMOVE_EFFECT, new EventListener<EffectEvent>() {
            @Override public void handle(EventType<EffectEvent> type, EffectEvent event) {
                if (event.creature != result.target || event.effect != result.effectToApply)
                    return;
                result.target.world.dispatcher.remove(Creature.REMOVE_EFFECT, this);
                tile.addAction(Actions.sequence(
                    Actions.alpha(0, 0.25f),
                    Actions.removeActor()
                ));
            }
        });
        return future;
    }
}
