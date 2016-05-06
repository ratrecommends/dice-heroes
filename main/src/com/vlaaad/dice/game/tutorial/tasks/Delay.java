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

public class Delay extends TutorialTask {
    private final float duration;

    public Delay(float duration) {
        this.duration = duration;
    }

    @Override public void start(final Callback callback) {
        final Stage stage = resources.get("stage");
        stage.addAction(Actions.delay(duration, Actions.run(new Runnable() {
            @Override public void run() {
                callback.taskEnded();
            }
        })));
    }
}
