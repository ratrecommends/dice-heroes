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
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.ui.WindowManager;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.behaviours.RequestProcessor;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCreatureParams;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.view.GameMessage;
import com.vlaaad.dice.game.world.view.Tile;
import com.vlaaad.dice.ui.windows.CreatureInfoWindow;

/**
 * Created 14.01.14 by vlaaad
 */
public class UserAbilityCreatureProcessor extends RequestProcessor<Creature, AbilityCreatureParams> {

    private ClickListener clickListener;
    private ClickListener confirmTargetListener;
    private Image confirm;
    private Tile back;
    private GameMessage message;
    private World world;

    @Override public int preProcess(AbilityCreatureParams params) {
        return 1;
    }

    @Override public void cancel() {
        if (world == null)
            return;
        ViewController viewController = world.getController(ViewController.class);
        Group root = viewController.root;
        viewController.removeSelection(this);
        root.removeCaptureListener(clickListener);
        if (confirmTargetListener != null) root.removeCaptureListener(confirmTargetListener);
        if (confirm != null) confirm.remove();
        if (back != null) back.remove();
        message.hide();

        world = null;
        clickListener = null;
        confirmTargetListener = null;
        confirm = null;
        back = null;
        message = null;
    }

    @Override public IFuture<Creature> process(AbilityCreatureParams params) {
        final World world = params.creature.world;
        this.world = world;
        final Array<Creature> targets = params.available;

        final Future<Creature> future = new Future<Creature>();
        message = new GameMessage(Config.thesaurus.localize("game-select-target"), world.stage);
        Array<Grid2D.Coordinate> coordinates = new Array<Grid2D.Coordinate>();

        for (Creature target : targets) {
            coordinates.add(new Grid2D.Coordinate(target.getX(), target.getY()));
        }

        world.getController(ViewController.class).showSelection(this, coordinates, "selection/move");
        message.show();
        clickListener = new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (event.isCancelled() || WindowManager.instance.isShown(CreatureInfoWindow.class))
                    return;
                ViewController viewController = world.getController(ViewController.class);
                Vector2 c = viewController.stageToWorldCoordinates(new Vector2(event.getStageX(), event.getStageY()));
                if (!world.inBounds(c.x, c.y))
                    return;
                Creature result = null;
                for (Creature target : targets) {
                    if (target.getX() == c.x && target.getY() == c.y) {
                        result = target;
                        break;
                    }
                }
                if (result != null) {
                    final ClickListener mainListener = this;
                    viewController.root.removeCaptureListener(this);
                    confirm = new Image(Config.skin, "selection/turn-confirmation");
                    back = new Tile("selection/turn-confirmation-background");
                    confirm.setPosition(c.x * ViewController.CELL_SIZE, c.y * ViewController.CELL_SIZE);
                    back.setPosition(confirm.getX(), confirm.getY());
                    viewController.notificationLayer.addActor(confirm);
                    viewController.selectionLayer.addActor(back);
                    confirmTargetListener = createConfirmTargetListener(confirm, back, mainListener, result, c.x, c.y);
                    viewController.root.addCaptureListener(confirmTargetListener);

                }
            }

            private ClickListener createConfirmTargetListener(final Image confirm, final Tile back, final ClickListener mainListener, final Creature target, final float cellX, final float cellY) {
                return new ClickListener() {
                    @Override public void clicked(InputEvent event, float x, float y) {
                        if (event.isCancelled() || WindowManager.instance.isShown(CreatureInfoWindow.class)) {
                            return;
                        }
                        Vector2 cell = world.getController(ViewController.class).stageToWorldCoordinates(new Vector2(event.getStageX(), event.getStageY()));
                        confirm.remove();
                        back.remove();
                        world.getController(ViewController.class).root.removeCaptureListener(this);
                        if (cellX == (int) cell.x && cellY == (int) cell.y) {
                            UserAbilityCreatureProcessor.this.cancel();
                            future.happen(target);
                            event.cancel();
                        } else {
                            world.getController(ViewController.class).root.addCaptureListener(mainListener);
                            mainListener.clicked(event, x, y);
                        }

                    }
                };
            }
        };
        world.getController(ViewController.class).root.addCaptureListener(clickListener);
        return future;
    }
}
