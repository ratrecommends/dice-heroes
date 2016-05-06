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

package com.vlaaad.dice.game.world.view;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.dice.game.objects.WorldObject;
import com.vlaaad.dice.game.world.controllers.ViewController;

/**
 * Created 16.02.14 by vlaaad
 */
public class ViewScroller {

    public static final float CENTER_ON_TIME = 0.2f;

    public static final int BOTTOM = 45;
    public static final int LEFT = 8;
    public static final int RIGHT = 8;
    public static final int TOP = 20;

    private static final Vector2 tmp1 = new Vector2();
    private static final Vector2 tmp2 = new Vector2();

    private final ViewController viewController;
    private final Stage stage;
    private final Group root;

    private final boolean hScrollEnabled;
    private final boolean vScrollEnabled;
    private final float minRootX;
    private final float maxRootX;
    private final float minRootY;
    private final float maxRootY;
    private final DragListener listener;

    private boolean enabled = true;

    public ViewScroller(ViewController viewController) {
        this.viewController = viewController;
        stage = viewController.world.stage;
        root = viewController.root;

        hScrollEnabled = root.getWidth() > stage.getWidth() - LEFT - RIGHT;
        vScrollEnabled = root.getHeight() > stage.getHeight() - BOTTOM - TOP;

        minRootX = stage.getWidth() - RIGHT - root.getWidth();
        maxRootX = LEFT;
        minRootY = stage.getHeight() - TOP - root.getHeight();
        maxRootY = BOTTOM;

        stage.addListener(listener = new DragListener() {
            {
                setTapSquareSize(8);
            }

            @Override public void drag(InputEvent event, float x, float y, int pointer) {
                if (!enabled)
                    return;
                if (!event.getTarget().isDescendantOf(root) && event.getTarget() != stage.getRoot())
                    return;
                if (hScrollEnabled) root.setX(MathUtils.clamp(root.getX() - getDeltaX(), minRootX, maxRootX));
                if (vScrollEnabled) root.setY(MathUtils.clamp(root.getY() - getDeltaY(), minRootY, maxRootY));
            }
        });
    }

    public void disable() {
        enabled = false;
    }

    public void enable() {
        enabled = true;
    }

    public void centerOn(int worldX, int worldY) {
        centerOn(worldX + 0.5f, worldY + 0.5f);
    }

    public void centerOn(float x, float y) {
        Vector2 viewCoordinate = tmp1.set(
            x * ViewController.CELL_SIZE,
            y * ViewController.CELL_SIZE
        );
        Vector2 currentCenter = tmp2.set(stage.getWidth(), stage.getHeight()).scl(0.5f);
        Vector2 targetPosition = currentCenter.sub(viewCoordinate);
        clamp(targetPosition);
        root.clearActions();
        root.addAction(Actions.moveTo(
            hScrollEnabled ? targetPosition.x : root.getX(),
            vScrollEnabled ? targetPosition.y : root.getY(),
            CENTER_ON_TIME
        ));
    }

    public void centerOn(WorldObject worldObject) {
        centerOn(worldObject.getX(), worldObject.getY());
    }

    private void clamp(Vector2 position) {
        position.x = MathUtils.clamp(position.x, minRootX, maxRootX);
        position.y = MathUtils.clamp(position.y, minRootY, maxRootY);
    }

    public void centerOn(Grid2D.Coordinate coordinate) {
        centerOn(coordinate.x(), coordinate.y());
    }

    public void centerOnImmediately(float x, float y) {
        Vector2 viewCoordinate = tmp1.set(
            x * ViewController.CELL_SIZE,
            y * ViewController.CELL_SIZE
        );
        Vector2 currentCenter = tmp2.set(stage.getWidth(), stage.getHeight()).scl(0.5f);
        Vector2 targetPosition = currentCenter.sub(viewCoordinate);
        clamp(targetPosition);
        root.setPosition(
            hScrollEnabled ? targetPosition.x : root.getX(),
            vScrollEnabled ? targetPosition.y : root.getY()
        );
    }
    public void dispose() {
        stage.removeListener(listener);
    }
}
