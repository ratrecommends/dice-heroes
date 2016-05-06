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
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.vlaaad.common.gdx.scene2d.ParticleActor;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.imp.Potion;
import com.vlaaad.dice.game.actions.results.ITargetOwner;
import com.vlaaad.dice.game.actions.results.imp.PotionResult;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.view.IVisualizer;
import com.vlaaad.dice.game.world.view.ResultVisualizer;
import com.vlaaad.dice.game.world.view.WorldObjectView;
import com.vlaaad.dice.managers.SoundManager;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

/**
 * Created 13.03.14 by vlaaad
 */
public class PotionVisualizer implements IVisualizer<PotionResult> {
    private static final Vector2 tmp = new Vector2();

    private final ResultVisualizer visualizer;

    public PotionVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(final PotionResult result) {
        if (result.potionActionType == Potion.ActionType.drink) {
            final Future<Void> future = new Future<Void>();
            Group group = new Group();
            SoundManager.instance.playSound("potion-sip");
            Image image = new Image(Config.skin.getRegion("ability/" + result.ability.name + "-icon"));
            group.addActor(image);
            group.setPosition(result.creature.getX() * ViewController.CELL_SIZE + 3, result.creature.getY() * ViewController.CELL_SIZE);
            visualizer.viewController.effectLayer.addActor(group);
            group.getColor().a = 0;
            image.setOrigin(image.getWidth() / 2, image.getHeight() / 2);
            image.setRotation(90);
            image.addAction(sequence(
                delay(0.2f),
                rotateBy(45, 0.2f)
            ));
            group.addAction(sequence(
                parallel(
                    moveBy(0, 24, 0.2f),
                    alpha(1, 0.2f)
                ),
                delay(0.2f),
                parallel(
                    moveBy(0, -10, 0.2f),
                    alpha(0, 0.2f)
                ),
                run(new Runnable() {
                    @Override public void run() {
                        visualizer.visualize(result.result).addListener(future);
                    }
                }),
                removeActor()
            ));
            return future;
        } else {
            if (result.result instanceof ITargetOwner) {
                final Creature target = ((ITargetOwner) result.result).getTarget();
                if (target == null || target.world == null) {
                    return visualizer.visualize(result.result);
                }
                visualizer.viewController.scroller.centerOn(target);
                final Future<Void> future = new Future<Void>();
                Group group = new Group();
                Image image = new Image(Config.skin.getRegion("ability/" + result.ability.name + "-icon"));
                group.addActor(image);
                group.setPosition(
                    result.creature.getX() * ViewController.CELL_SIZE + 6,
                    result.creature.getY() * ViewController.CELL_SIZE
                );
                visualizer.viewController.effectLayer.addActor(group);
                image.setOrigin(image.getWidth() / 2, image.getHeight() / 2);
                image.addAction(rotateBy(720, 1f));
                WorldObjectView from = visualizer.viewController.getView(result.creature);
                WorldObjectView to = visualizer.viewController.getView(target);
//                Vector2 middle = new Vector2(from.getX(), from.getY()).lerp(new Vector2(to.getX(), to.getY()), 0.5f);

                float dx = from.getX() - to.getX();
                float dy = from.getY() - to.getY();
                group.addAction(sequence(
                    moveTo(to.getX(), to.getY() + 6, tmp.set(dx, dy).len() * 0.005f),
                    run(new Runnable() {
                        @Override public void run() {
                            ParticleActor actor = new ParticleActor("potion-broken-bottle");
                            SoundManager.instance.playSound("broken-glass");
                            actor.freeOnComplete();
                            actor.setPosition(
                                target.getX() * ViewController.CELL_SIZE + ViewController.CELL_SIZE * 0.5f,
                                target.getY() * ViewController.CELL_SIZE + ViewController.CELL_SIZE * 0.5f + 6
                            );
                            visualizer.viewController.effectLayer.addActor(actor);
                            visualizer.visualize(result.result).addListener(future);
                        }
                    }),
                    removeActor()
                ));
                return future;
            } else {
                return visualizer.visualize(result.result);
            }
        }
    }
}
