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
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.vlaaad.common.gdx.scene2d.ParticleActor;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.actions.results.imp.IceStormResult;
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

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public class IceStormVisualizer implements IVisualizer<IceStormResult> {

    private static final Vector2 tmp = new Vector2();

    private final ResultVisualizer visualizer;

    public IceStormVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(final IceStormResult result) {
        final Future<Void> future = new Future<Void>();
        final ParticleActor particleActor = new ParticleActor("ability-" + result.ability.name).freeOnComplete();
        particleActor.getColor().a = 0;
        final Vector2 position = tmp.set(result.coordinate.x() + 0.5f, result.coordinate.y() + 0.5f).scl(ViewController.CELL_SIZE);
        particleActor.setPosition(position.x, position.y + 160);
        visualizer.viewController.effectLayer.addActor(particleActor);
        SoundManager.instance.playSound("ability-fireball");
        visualizer.viewController.scroller.centerOn(result.coordinate.x(), result.coordinate.y());
        particleActor.addAction(alpha(1f, 0.5f));
        particleActor.addAction(sequence(
            moveTo(position.x, position.y, 1f),
            run(new Runnable() {
                @Override public void run() {
                    particleActor.effect.allowCompletion();
                    final ParticleActor hitActor = new ParticleActor("ability-" + result.ability.name + "-hit").freeOnComplete();
                    hitActor.setPosition(position.x, position.y);
                    visualizer.viewController.spawnLayer.addActor(hitActor);
                    SoundManager.instance.playSound("ability-freeze-hit");
                    visualizer.viewController.world.stage.addAction(Actions.delay(0.5f, Actions.run(future)));
                    if (result.targets.size == 0)
                        return;
                    for (ObjectIntMap.Entry<Creature> e : result.targets) {
                        showFreeze(e.key);
                    }
                }
            })
        ));
        return future;
    }

    private void showFreeze(final Creature creature) {
        final TileSubView freeze = new TileSubView("creature-effect-freeze");
        freeze.getActor().getColor().a = 0f;
        final WorldObjectView view = visualizer.viewController.getView(creature);
        view.addSubView(freeze);
        freeze.getActor().addAction(alpha(1, 0.5f));
        visualizer.viewController.world.dispatcher.add(Creature.REMOVE_EFFECT, new EventListener<EffectEvent>() {
            @Override public void handle(EventType<EffectEvent> type, EffectEvent event) {
                if (event.creature == creature && event.effect instanceof FreezeEffect) {
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
}
