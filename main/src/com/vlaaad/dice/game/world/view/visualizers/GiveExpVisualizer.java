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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.vlaaad.common.gdx.scene2d.AnimatedActor;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.common.ui.WindowListener;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.results.imp.GiveExpResult;
import com.vlaaad.dice.game.config.professions.ProfessionDescription;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.view.IVisualizer;
import com.vlaaad.dice.game.world.view.ResultVisualizer;
import com.vlaaad.dice.game.world.view.TileSubView;
import com.vlaaad.dice.game.world.view.WorldObjectView;
import com.vlaaad.dice.managers.SoundManager;
import com.vlaaad.dice.ui.windows.LevelUpWindow;

/**
 * Created 12.01.14 by vlaaad
 */
public class GiveExpVisualizer implements IVisualizer<GiveExpResult> {
    private final ResultVisualizer visualizer;

    public GiveExpVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(final GiveExpResult result) {
        if (result.exp == 0)
            return Future.completed();
        if (result.creature.initialPlayer != visualizer.viewController.world.viewer) {
            return Future.completed();
        }
        final Future<Void> future = new Future<Void>();
        final ViewController viewController = visualizer.viewController;
        final WorldObjectView view = viewController.getView(result.creature);
        String text = "+" + result.exp + " exp";
        final ProfessionDescription profession = result.creature.description.profession;
        final int currentExp = result.creature.description.exp + result.creature.gainedExp;
        final Label label = new Label(text, Config.skin, "default", Color.WHITE);
        label.setPosition(
            view.getX() + view.getWidth() / 2 - label.getPrefWidth() / 2,
            view.getY() + view.getHeight() / 2 - label.getPrefHeight() / 2
        );
        label.addAction(Actions.sequence(
            Actions.parallel(
                Actions.alpha(0, 1f),
                Actions.moveBy(0, ViewController.CELL_SIZE / 2, 1f)
            ),
            Actions.run(new Runnable() {
                @Override public void run() {
                    label.remove();
                    if (profession.getLevel(currentExp) == profession.getLevel(currentExp + result.exp)) {
                        future.happen();
                        return;
                    }
                    final AnimatedActor circle = new AnimatedActor(0.1f, Config.skin.getAtlas().findRegions("animation/levelup-circle"));
                    circle.setPosition(
                        view.getX() + view.getWidth() / 2 - circle.getWidth() / 2,
                        view.getY() + view.getHeight() / 2 - circle.getHeight() / 2
                    );
                    viewController.selectionLayer.addActor(circle);
                    circle.addListener(new ChangeListener() {
                        @Override public void changed(ChangeEvent event, Actor actor) {
                            circle.remove();
                            final TileSubView image = new TileSubView("animation/levelup-white-cube", -1);
                            view.addSubView(image);
                            image.getActor().getColor().a = 0;
                            image.getActor().addAction(Actions.sequence(
                                Actions.alpha(1f, 0.4f),
                                Actions.run(new Runnable() {
                                    @Override public void run() {
                                        SoundManager.instance.playMusicAsSound("level-up");
                                        view.addAction(Actions.sequence(
                                            Actions.moveBy(0, 20, 0.3f, Interpolation.exp10Out),
                                            Actions.run(new Runnable() {
                                                @Override public void run() {
                                                    view.addAction(
                                                        Actions.sequence(
                                                            Actions.delay(0.2f),
                                                            Actions.run(new Runnable() {
                                                                @Override public void run() {
                                                                    Label levelUp = new Label("LEVEL UP!", Config.skin, "default", Color.WHITE);
                                                                    levelUp.setPosition(
                                                                        (result.creature.getX() + 0.5f) * ViewController.CELL_SIZE - levelUp.getWidth() / 2,
                                                                        (result.creature.getY() + 0.5f) * ViewController.CELL_SIZE
                                                                    );
                                                                    viewController.notificationLayer.addActor(levelUp);
                                                                    levelUp.addAction(Actions.moveBy(0, 45, 1.5f));
                                                                    levelUp.addAction(Actions.sequence(
                                                                        Actions.alpha(0, 1.5f),
                                                                        Actions.removeActor()
                                                                    ));

                                                                }
                                                            })
                                                        )
                                                    );
                                                }
                                            }),
                                            Actions.moveBy(0, -20, 0.4f, new Interpolation.BounceOut(2)),
                                            Actions.run(new Runnable() {
                                                @Override public void run() {
                                                    image.getActor().addAction(Actions.sequence(
                                                        Actions.alpha(0, 0.4f),
                                                        Actions.run(new Runnable() {
                                                            @Override public void run() {
                                                                image.getActor().remove();
                                                                GameWindow<GiveExpResult> window = new LevelUpWindow();
                                                                window.addListener(new WindowListener() {
                                                                    @Override protected void hidden(WindowEvent event) {
                                                                        future.happen();
                                                                    }
                                                                });
                                                                window.show(result);
                                                            }
                                                        })
                                                    ));
                                                }
                                            })
                                        ));
                                    }
                                })
                            ));
                        }
                    });
                }
            })
        ));
        viewController.notificationLayer.addActor(label);
        return future;
    }
}
