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

import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.vlaaad.common.tutorial.TutorialTask;

/**
 * Created 15.11.13 by vlaaad
 */
public class AllowKeyPresses extends TutorialTask {
    private final int[] keyCodes;

    public AllowKeyPresses(int... keyCodes) {
        super();
        this.keyCodes = keyCodes;
    }

    @Override public void start(Callback callback) {
        for (int keyCode : keyCodes) {
            EventListener listener = resources.getIfExists("restrictKeyPress" + keyCode);
            if (listener != null) {
                Stage stage = resources.get("stage");
                stage.removeCaptureListener(listener);
                resources.remove("restrictKeyPress" + keyCode);
            }
        }
        callback.taskEnded();
    }
}
