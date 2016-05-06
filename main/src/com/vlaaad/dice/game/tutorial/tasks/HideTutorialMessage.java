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

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.vlaaad.common.tutorial.TutorialTask;

/**
 * Created 13.11.13 by vlaaad
 */
public class HideTutorialMessage extends TutorialTask {
    private final boolean immediately;

    public HideTutorialMessage() {
        this(false);
    }

    public HideTutorialMessage(boolean immediately) {
        this.immediately = immediately;
    }

    @Override public void start(Callback callback) {
        ShowTutorialMessage.Message message = resources.getIfExists("tutorialMessage");
        if (message == null) {
            callback.taskEnded();
            return;
        }
        resources.remove("tutorialMessage");
        if (immediately) {
            message.remove();
            callback.taskEnded();
            return;
        }

        Stage stage = resources.get("stage");

        if (message.getStage() != stage) {
            message.remove();
            callback.taskEnded();
        }

        message.clearActions();
        message.addAction(Actions.sequence(
            Actions.moveTo(0, message.onTop ? message.child.getPrefHeight() : -message.child.getPrefHeight(), 0.2f),
            Actions.removeActor()
        ));

        callback.taskEnded();
    }
}
