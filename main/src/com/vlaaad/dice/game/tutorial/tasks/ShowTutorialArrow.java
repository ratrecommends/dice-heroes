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

package com.vlaaad.dice.game.tutorial.tasks;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.vlaaad.common.tutorial.TutorialTask;
import com.vlaaad.dice.Config;

/**
 * Created 13.11.13 by vlaaad
 */
public abstract class ShowTutorialArrow extends TutorialTask {
    private static final Vector2 tmp = new Vector2();

    @Override public final void start(Callback callback) {

        final Actor target = getTarget();
        final Stage stage = target.getStage();
        if (stage == null)
            throw new IllegalStateException("target is not on stage");
        Image arrow = resources.getIfExists("tutorialArrow");
        if (arrow == null) {
            arrow = new Image(Config.skin.getRegion("ui-tutorial-arrow"));
            arrow.setOrigin(arrow.getWidth() / 2, arrow.getHeight() / 2);
            stage.addActor(arrow);
            resources.put("tutorialArrow", arrow);
        } else {
            arrow.toFront();
        }
        arrow.clearActions();
        Vector2 screenPosition = target.localToStageCoordinates(tmp.set(0, 0));
        ArrowDirection direction = getDirection();
        switch (direction) {
            case fromLeft:
                arrow.setPosition(
                    screenPosition.x - arrow.getPrefWidth() - getArrowOffset(),
                    screenPosition.y + target.getHeight() / 2f - arrow.getPrefHeight() / 2f
                );
                arrow.setRotation(-90);
                break;
            case fromRight:
                arrow.setPosition(
                    screenPosition.x + target.getWidth() + getArrowOffset() - arrow.getPrefWidth() / 2,
                    screenPosition.y + target.getHeight() / 2f - arrow.getPrefHeight() / 2f
                );
                arrow.setRotation(90);
                break;
            case fromTop:
                arrow.setPosition(
                    screenPosition.x + target.getWidth() / 2f - arrow.getPrefWidth() / 2f,
                    screenPosition.y + target.getHeight() + getArrowOffset()
                );
                arrow.setRotation(180);
                break;
            case fromBottom:
                arrow.setPosition(
                    screenPosition.x + target.getWidth() / 2f - arrow.getPrefWidth() / 2f,
                    screenPosition.y - arrow.getPrefHeight() - getArrowOffset()
                );
                arrow.setRotation(0);
                break;
            default:
                throw new IllegalStateException("unknown direction: " + direction);
        }
        blink(arrow, direction);
        callback.taskEnded();
    }

    private void blink(final Image arrow, final ArrowDirection direction) {
        if (direction == ArrowDirection.fromTop || direction == ArrowDirection.fromBottom) {
            arrow.addAction(Actions.sequence(
                Actions.moveBy(0, moveOffset(), periodTime() * 0.25f),
                Actions.moveBy(0, -moveOffset() * 2f, periodTime() * 0.5f),
                Actions.moveBy(0, moveOffset(), periodTime() * 0.25f),
                Actions.run(new Runnable() {
                    @Override public void run() {
                        blink(arrow, direction);
                    }
                })
            ));
        } else {
            arrow.addAction(Actions.sequence(
                Actions.moveBy(moveOffset(), 0, periodTime() * 0.25f),
                Actions.moveBy(-moveOffset() * 2f, 0 * 2f, periodTime() * 0.5f),
                Actions.moveBy(moveOffset(), 0, periodTime() * 0.25f),
                Actions.run(new Runnable() {
                    @Override public void run() {
                        blink(arrow, direction);
                    }
                })
            ));
        }
    }

    protected float moveOffset() { return 3f; }

    protected float periodTime() { return 0.75f; }

    protected float getArrowOffset() { return 3f; }

    protected abstract Actor getTarget();

    protected abstract ArrowDirection getDirection();

    public static enum ArrowDirection {
        fromLeft, fromRight, fromTop, fromBottom
    }
}
