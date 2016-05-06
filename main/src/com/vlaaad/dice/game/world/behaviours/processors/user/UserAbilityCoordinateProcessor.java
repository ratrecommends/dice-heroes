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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.behaviours.RequestProcessor;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCoordinatesParams;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.view.GameMessage;
import com.vlaaad.dice.game.world.view.Tile;

/**
 * Created 14.01.14 by vlaaad
 */
public class UserAbilityCoordinateProcessor extends RequestProcessor<Grid2D.Coordinate, AbilityCoordinatesParams> {
    private ClickListener mainListener;
    private ClickListener additionalListener;
    private World world;
    private GameMessage message;
    private Image confirm;
    private Tile back;
    @Override public int preProcess(AbilityCoordinatesParams params) {
        return 1;
    }

    @Override public void cancel() {
        if (world == null)
            return;
        world.stage.removeListener(mainListener);
        if (additionalListener != null)
            world.stage.removeListener(additionalListener);
        if (confirm != null) confirm.remove();
        if (back != null) back.remove();
        world.getController(ViewController.class).removeSelection(UserAbilityCoordinateProcessor.this);
        message.hide();

        world = null;
        confirm = null;
        back = null;
        message = null;
        mainListener = null;
        additionalListener = null;
    }

    @Override public IFuture<Grid2D.Coordinate> process(final AbilityCoordinatesParams params) {
        final Future<Grid2D.Coordinate> future = new Future<Grid2D.Coordinate>();
        final World world = params.creature.world;
        this.world = world;
        world.getController(ViewController.class).showSelection(this, params.coordinates, "selection/move");
        message = new GameMessage(Config.thesaurus.localize("game-select-cell"), world.stage);
        message.show();
        mainListener = new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (event.isCancelled())
                    return;
                Vector2 c = world.getController(ViewController.class).stageToWorldCoordinates(new Vector2(event.getStageX(), event.getStageY()));
                if (!world.inBounds(c.x, c.y))
                    return;
                final int cellX = (int) c.x;
                final int cellY = (int) c.y;
                boolean isAvailable = false;
                for (Grid2D.Coordinate coordinate : params.coordinates) {
                    if (coordinate.x() == cellX && coordinate.y() == cellY) {
                        isAvailable = true;
                        break;
                    }
                }
                if (!isAvailable)
                    return;
                world.stage.removeListener(this);
                final ClickListener mainListener = this;
                confirm = new Image(Config.skin, "selection/turn-confirmation");
                back = new Tile("selection/turn-confirmation-background");
                confirm.setPosition(cellX * ViewController.CELL_SIZE, cellY * ViewController.CELL_SIZE);
                back.setPosition(confirm.getX(), confirm.getY());
                world.getController(ViewController.class).notificationLayer.addActor(confirm);
                world.getController(ViewController.class).selectionLayer.addActor(back);
                additionalListener = new ClickListener() {
                    @Override public void clicked(InputEvent event, float x, float y) {
                        if (event.isCancelled())
                            return;
                        Vector2 cell = world.getController(ViewController.class).stageToWorldCoordinates(new Vector2(event.getStageX(), event.getStageY()));
                        confirm.remove();
                        back.remove();
                        world.stage.removeListener(this);
                        if (cellX == (int) cell.x && cellY == (int) cell.y) {
                            UserAbilityCoordinateProcessor.this.cancel();
                            future.happen(new Grid2D.Coordinate(cellX, cellY));
                        } else {
                            world.stage.addListener(mainListener);
                            mainListener.clicked(event, x, y);
                        }
                    }
                };
                world.stage.addListener(additionalListener);
            }
        };
        world.stage.addListener(mainListener);
        return future;
    }
}
