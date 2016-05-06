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

package com.vlaaad.common.tutorial.tasks;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.vlaaad.common.tutorial.TutorialTask;

/**
 * Created 11.11.13 by vlaaad
 */
public abstract class ArrowForceClick extends TutorialTask {

    @Override public void start(final Callback callback) {
        init();
        final Actor target = getTargetActor();
        final Image arrow = new Image(getArrowDrawable());
        final Table message = getMessageTable();
        final Stage stage = target.getStage();
        ArrowDirection direction = getDirection();
        if (stage == null)
            throw new IllegalStateException("target is not on stage");
        addListener(stage, target, arrow, message, callback);
        stage.addActor(arrow);
        stage.addActor(message);
        Vector2 screenPosition = target.localToStageCoordinates(new Vector2());
        switch (direction) {
            case left:
                arrow.setPosition(
                    screenPosition.x - arrow.getPrefWidth() - getArrowOffset(),
                    screenPosition.y + target.getHeight() / 2f - arrow.getPrefHeight() / 2f
                );
                message.setSize(arrow.getX() - getArrowOffset(), stage.getHeight());
                break;
            case right:
                arrow.setPosition(
                    screenPosition.x + target.getWidth() + getArrowOffset(),
                    screenPosition.y + target.getHeight() / 2f - arrow.getPrefHeight() / 2f
                );
                message.setSize(stage.getWidth() - arrow.getX() - arrow.getPrefWidth() - getArrowOffset(), stage.getHeight());
                message.setX(arrow.getX() + arrow.getPrefWidth() + getArrowOffset());
                break;
            case top:
                arrow.setPosition(
                    screenPosition.x + target.getWidth() / 2f - arrow.getPrefWidth() / 2f,
                    screenPosition.y + target.getHeight() + getArrowOffset()
                );
                message.setSize(stage.getWidth(), stage.getHeight() - arrow.getX() - arrow.getPrefHeight() - getArrowOffset());
                message.setY(arrow.getY() + arrow.getPrefHeight() + getArrowOffset());
                break;
            case bottom:
                arrow.setPosition(
                    screenPosition.x + target.getWidth() / 2f - arrow.getPrefWidth() / 2f,
                    screenPosition.y - arrow.getPrefHeight() - getArrowOffset()
                );
                message.setSize(stage.getWidth(), arrow.getY() - getArrowOffset());
                break;
            default:
                throw new IllegalStateException("unknown direction: " + direction);
        }

    }

    protected void addListener(final Stage stage, final Actor target, final Image arrow, final Table message, final Callback callback) {
        stage.addCaptureListener(new InputListener() {
            @Override public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Actor result = stage.hit(event.getStageX(), event.getStageY(), true);
                if (!result.isDescendantOf(target)) {
                    event.cancel();
                    return false;
                }
                return true;
            }

            @Override public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Actor result = stage.hit(event.getStageX(), event.getStageY(), true);
                if (!result.isDescendantOf(target)) {
                    event.cancel();
                    return;
                }
                stage.removeCaptureListener(this);
                arrow.remove();
                message.remove();
                callback.taskEnded();
            }
        });
    }

    protected float getArrowOffset() { return 1f; }

    protected abstract void init();

    protected abstract Actor getTargetActor();

    protected abstract Drawable getArrowDrawable();

    protected abstract ArrowDirection getDirection();

    protected abstract Table getMessageTable();

    public static enum ArrowDirection {
        left, right, top, bottom
    }
}
