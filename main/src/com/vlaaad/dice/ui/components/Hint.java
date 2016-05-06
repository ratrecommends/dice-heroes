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

package com.vlaaad.dice.ui.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.ui.scene2d.LocLabel;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

/**
 * Created 28.06.14 by vlaaad
 */
public class Hint {
    private static final Color TEXT_COLOR = new Color(0, 0, 0, 0.75f);

    public static final int TOUCH_OFFSET = 10;
    public static final int SCREEN_EDGE_OFFSET = 0;

    private static final Vector2 tmp = new Vector2();

    private final Cell labelCell;
    private final Table table;
    private final LocLabel label;

    private boolean shown = false;

    public Hint(String locKey) {
        this(locKey, Thesaurus.EMPTY);
    }

    public Hint(String locKey, ObjectMap<String, String> params) {
        label = new LocLabel(locKey, params, TEXT_COLOR);
        label.setWrap(true);
        label.setAlignment(Align.center);
        table = new Table(Config.skin);
        table.setBackground("ui/hint-background");
        labelCell = table.add(label).expand().fill().minWidth(60);
    }

    public void show(Stage stage, float stageX, float stageY) {
        if (shown || stage == null)
            return;
        shown = true;
        stage.addActor(table);
        updateTableWidth(Float.MAX_VALUE);
        // if actor is too high, try to place it left or right
        if (table.getHeight() + stageY + TOUCH_OFFSET > stage.getHeight() - SCREEN_EDGE_OFFSET) {
            if (stageX > stage.getWidth() / 2) {
                //left
                updateTableWidth(stageX - TOUCH_OFFSET - SCREEN_EDGE_OFFSET);
                table.setX(stageX - TOUCH_OFFSET - table.getWidth());
            } else {
                //right
                updateTableWidth(stage.getWidth() - stageX - TOUCH_OFFSET - SCREEN_EDGE_OFFSET);
                table.setX(stageX + TOUCH_OFFSET);
            }
            table.setY(MathUtils.clamp(stageY - table.getHeight() / 2, SCREEN_EDGE_OFFSET, stage.getHeight() - table.getHeight() - SCREEN_EDGE_OFFSET));
        } else {
            table.setPosition(
                MathUtils.clamp(stageX - table.getWidth() / 2, SCREEN_EDGE_OFFSET, stage.getWidth() - table.getWidth() - SCREEN_EDGE_OFFSET),
                stageY + TOUCH_OFFSET
            );
        }
        table.clearActions();
        table.getColor().a = 0;
        table.addAction(alpha(1, 0.25f));
    }

    private void updateTableWidth(float value) {
        value = Math.min(value - table.getPadLeft() - table.getPadRight(), 100 - table.getPadLeft() - table.getPadRight());
        label.setWidth(value);
        label.invalidate();
        label.validate();
        label.layout();
        labelCell.prefWidth(value);
        table.setWidth(value + table.getPadLeft() + table.getPadRight());
        table.setHeight(label.getPrefHeight() + table.getPadTop() + table.getPadBottom());
    }

    public boolean hide() {
        if (!shown)
            return false;
        shown = false;
        table.clearActions();
        table.addAction(sequence(
            alpha(0, 0.25f),
            removeActor()
        ));
        return true;
    }

    public static Hint make(Actor actor, String locKey) {
        return make(actor, locKey, Thesaurus.EMPTY);

    }
    public static Hint make(final Actor actor, String locKey, ObjectMap<String, String> params) {
        final Hint result = new Hint(locKey, params);

        actor.addCaptureListener(new ActorGestureListener(20, 0.4f, 0.3f, 0.15f) {

            @Override public boolean longPress(Actor actor, float x, float y) {
                if (actor.getStage() == null)
                    return false;
                actor.localToStageCoordinates(tmp.set(x, y));
                result.show(actor.getStage(), tmp.x, tmp.y);
                return false;
            }

            @Override public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
                if (result.hide())
                    event.cancel();
            }
            @Override public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (result.hide()) {
                    event.cancel();
                    if (actor.getStage() != null) actor.getStage().cancelTouchFocus();
                }
            }
        });
        return result;
    }
}
