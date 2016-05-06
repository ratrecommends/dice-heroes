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

package com.vlaaad.common.tutorial;

import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;

import java.util.Arrays;
import java.util.List;

/**
 * Created 13.11.13 by vlaaad
 */
public class RestrictKeyPresses extends TutorialTask {
    private final int[] keyCodes;

    public RestrictKeyPresses(int... keyCodes) {
        super();
        this.keyCodes = keyCodes;
        Arrays.sort(this.keyCodes);
    }

    @Override public void start(Callback callback) {
        EventListener listener = new InputListener() {
            @Override public boolean keyDown(InputEvent event, int keycode) {
                if (Arrays.binarySearch(keyCodes, keycode) >= 0) {
                    event.cancel();
                    return true;
                }
                return super.keyDown(event, keycode);
            }

            @Override public boolean keyUp(InputEvent event, int keycode) {
                if (Arrays.binarySearch(keyCodes, keycode) >= 0) {
                    event.cancel();
                    return true;
                }
                return super.keyUp(event, keycode);
            }
        };
        Stage stage = resources.get("stage");
        stage.addCaptureListener(listener);
        for (int keyCode : keyCodes) {
            resources.put("restrictKeyPress" + keyCode, listener);
        }
        callback.taskEnded();
    }
}
