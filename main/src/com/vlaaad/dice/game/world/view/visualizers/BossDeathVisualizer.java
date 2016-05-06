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

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.tutorial.RestrictKeyPresses;
import com.vlaaad.common.tutorial.Tutorial;
import com.vlaaad.common.tutorial.tasks.ForceClickStage;
import com.vlaaad.common.util.ArrayHelper;
import com.vlaaad.common.util.Function;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.game.actions.results.ITargetOwner;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.tutorial.DiceTutorial;
import com.vlaaad.dice.game.tutorial.tasks.AllowKeyPresses;
import com.vlaaad.dice.game.tutorial.tasks.HideTutorialMessage;
import com.vlaaad.dice.game.tutorial.tasks.ShowTutorialMessage;
import com.vlaaad.dice.game.world.controllers.RoundController;
import com.vlaaad.dice.game.world.players.PlayerRelation;
import com.vlaaad.dice.game.world.view.IVisualizer;
import com.vlaaad.dice.game.world.view.ResultVisualizer;
import com.vlaaad.dice.game.world.view.WorldObjectView;
import com.vlaaad.dice.game.world.view.visualizers.objects.Death;

public class BossDeathVisualizer implements IVisualizer<Death> {
    private final ResultVisualizer visualizer;

    public BossDeathVisualizer(ResultVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    @Override public IFuture<Void> visualize(Death death) {
        final Future<Void> future = Future.make();
        final Creature target = death.target;
        final WorldObjectView view = visualizer.viewController.getView(target);
        view.addAction(Actions.sequence(
            Actions.alpha(0.5f),
            Actions.delay(0.1f),
            Actions.run(new Runnable() {
                @Override public void run() {
                    final Tutorial.TutorialResources resources = Tutorial
                        .resources()
                        .with("stage", visualizer.viewController.root.getStage());

                    new Tutorial(resources, DiceTutorial.tasks(
                        new RestrictKeyPresses(Input.Keys.ESCAPE, Input.Keys.MENU, Input.Keys.BACK),
                        new ShowTutorialMessage("tutorial-boss-death", true, true).withParams(new Function<Tutorial.TutorialResources, ObjectMap<String, String>>() {
                            @Override public ObjectMap<String, String> apply(Tutorial.TutorialResources res) {
                                final Array<Creature> alive = ArrayHelper.from(visualizer.viewController.world.getController(RoundController.class).getAlive(PlayerRelation.self));
                                final Thesaurus.Params params = Thesaurus.params();
                                if (alive.size == 0) {
                                    params.put("name", "pudi");
                                } else {
                                    for (Creature creature : alive) {
                                        if (creature.name.equals("pudi") || !params.containsKey("name")) {
                                            params.put("name", creature.description.nameLocKey());
                                        }
                                    }
                                }
                                return params;
                            }
                        }),
                        new ForceClickStage(),
                        new HideTutorialMessage(),
                        new AllowKeyPresses(Input.Keys.ESCAPE, Input.Keys.MENU, Input.Keys.BACK)
                    ))
                        .start()
                        .addListener(new IFutureListener<Tutorial>() {
                            @Override public void onHappened(Tutorial tutorial) {
                                view.addAction(Actions.sequence(
                                    Actions.alpha(0f),
                                    Actions.delay(0.1f),
                                    Actions.alpha(0.5f),
                                    Actions.delay(0.1f),
                                    Actions.alpha(0f),
                                    Actions.delay(0.1f),
                                    Actions.alpha(0.5f),
                                    Actions.delay(0.1f),
                                    Actions.alpha(0f),
                                    Actions.run(future)
                                ));
                            }
                        });
                }
            })
        ));
        return future;
    }
}
