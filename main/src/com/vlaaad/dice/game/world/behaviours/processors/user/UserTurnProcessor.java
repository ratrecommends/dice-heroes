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

package com.vlaaad.dice.game.world.behaviours.processors.user;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.vlaaad.common.tutorial.Tutorial;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.common.util.Tuple2;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.imp.Potion;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.tutorial.DiceTutorial;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.behaviours.RequestProcessor;
import com.vlaaad.dice.game.world.behaviours.params.TurnParams;
import com.vlaaad.dice.game.world.behaviours.responces.TurnResponse;
import com.vlaaad.dice.game.world.controllers.UiController;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.view.Tile;
import com.vlaaad.dice.ui.components.ProfessionAbilityIcon;
import com.vlaaad.dice.ui.windows.PotionsPlayWindow;
import com.vlaaad.dice.ui.windows.ProfessionAbilityPlayWindow;

/**
 * Created 14.01.14 by vlaaad
 */
public class UserTurnProcessor extends RequestProcessor<TurnResponse, TurnParams> {

    private World world;
    private ClickListener clickListener;
    private ClickListener confirmListener;
    private Image confirm;
    private Tile back;

    @Override public int preProcess(TurnParams params) { return 1; }

    @Override public void cancel() {
        if (world == null)
            return;
        world.stage.removeListener(clickListener);
        if (confirmListener != null) world.stage.removeListener(confirmListener);
        if (confirm != null) confirm.remove();
        if (back != null) back.remove();
        UiController ui = world.getController(UiController.class);
        //can be null in tutorial
        if (ui != null) ui.disableActionButtons();
        world = null;
        confirmListener = null;
        clickListener = null;
        confirm = null;
        back = null;
    }

    @Override public IFuture<TurnResponse> process(final TurnParams params) {
        final Future<TurnResponse> future = new Future<TurnResponse>();
        final World world = params.creature.world;
        this.world = world;
        final Creature creature = params.creature;
        final UiController ui = world.getController(UiController.class);
        final ObjectSet<Grid2D.Coordinate> available = new ObjectSet<Grid2D.Coordinate>();
        available.addAll(params.availableCells);

        clickListener = new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (event.isCancelled())
                    return;
                Vector2 c = world.getController(ViewController.class).stageToWorldCoordinates(new Vector2(event.getStageX(), event.getStageY()));
                final Grid2D.Coordinate coordinate = new Grid2D.Coordinate((int) c.x, (int) c.y);
                if (available.contains(coordinate)) {
                    world.stage.removeListener(this);
                    final ClickListener mainListener = this;
                    confirm = new Image(Config.skin, "selection/turn-confirmation");
                    back = new Tile("selection/turn-confirmation-background");
                    confirm.setPosition(coordinate.x() * ViewController.CELL_SIZE, coordinate.y() * ViewController.CELL_SIZE);
                    back.setPosition(confirm.getX(), confirm.getY());
                    world.getController(ViewController.class).notificationLayer.addActor(confirm);
                    world.getController(ViewController.class).selectionLayer.addActor(back);
                    confirmListener = new ClickListener() {
                        @Override public void clicked(InputEvent event, float x, float y) {
                            if (event.isCancelled())
                                return;
                            Vector2 cell = world.getController(ViewController.class).stageToWorldCoordinates(new Vector2(event.getStageX(), event.getStageY()));
                            confirm.remove();
                            back.remove();
                            world.stage.removeListener(this);
                            if (coordinate.x() == (int) cell.x && coordinate.y() == (int) cell.y) {
                                UserTurnProcessor.this.cancel();
                                future.happen(new TurnResponse<Grid2D.Coordinate>(TurnResponse.TurnAction.MOVE, coordinate));
                            } else {
                                world.stage.addListener(mainListener);
                                mainListener.clicked(event, x, y);
                            }
                        }
                    };
                    world.stage.addListener(confirmListener);
                }
            }
        };
        world.stage.addListener(clickListener);

        Array<Ability> professionAbilities = creature.description.profession.getAvailableAbilities(creature.getCurrentLevel());
        boolean professionTutorial = false;
        if (professionAbilities.size > 0) {
            for (Ability ability : professionAbilities) {
                ProfessionAbilityIcon icon = ui.getProfessionIcon(ability);
                icon.addListener(createListener(ability, creature, clickListener, world, future));
            }
            if (!world.viewer.tutorialProvider.isProfessionAbilitiesTutorialCompleted()) {
                professionTutorial = true;
                Tutorial.whenAllTutorialsEnded(new Runnable() {
                    @Override public void run() {
                        Tutorial.TutorialResources resources = Tutorial.resources()
                            .with("tutorial-provider", world.viewer.tutorialProvider)
                            .with("world", world)
                            .with("stage", world.stage);
                        new Tutorial(resources, DiceTutorial.professionAbilitiesTasks()).start();
                    }
                });
            }
        }
        if (!professionTutorial && !world.viewer.tutorialProvider.isPlayPotionsTutorialCompleted() && world.viewer.hasPotions()) {
            Tutorial.whenAllTutorialsEnded(new Runnable() {
                @Override public void run() {
                    Tutorial.TutorialResources resources = Tutorial.resources()
                        .with("tutorial-provider", world.viewer.tutorialProvider)
                        .with("world", world)
                        .with("stage", world.stage);
                    new Tutorial(resources, DiceTutorial.playPotionsTasks()).start();
                }
            });
        }
        if (ui != null) {
            ui.potionsButton.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    new PotionsPlayWindow().show(new PotionsPlayWindow.Params(creature, world, new PotionsPlayWindow.Callback() {
                        @Override public void usePotion(Ability potion, Potion.ActionType action) {
                            UserTurnProcessor.this.cancel();
                            future.happen(new TurnResponse<Tuple2<Ability, Potion.ActionType>>(TurnResponse.TurnAction.POTION, Tuple2.make(potion, action)));
                        }
                    }));
                }
            });
        }


        return future;
    }

    private EventListener createListener(final Ability ability, final Creature creature, final ClickListener clickListener, final World world, final Future<TurnResponse> future) {
        return new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                new ProfessionAbilityPlayWindow().show(new ProfessionAbilityPlayWindow.Params(ability, creature, new ProfessionAbilityPlayWindow.Callback() {
                    @Override public void onUseAbility() {
                        UserTurnProcessor.this.cancel();
                        future.happen(new TurnResponse<Ability>(TurnResponse.TurnAction.PROFESSION_ABILITY, ability));
                    }
                }));
            }
        };
    }


}
