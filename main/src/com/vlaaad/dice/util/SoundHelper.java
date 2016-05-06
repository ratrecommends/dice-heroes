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

package com.vlaaad.dice.util;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.vlaaad.dice.managers.SoundManager;

/**
 * Created 22.10.13 by vlaaad
 */
public class SoundHelper {
    private SoundHelper() {
    }

    public static void initButton(final Button value) {
        value.addListener(new InputListener() {
            @Override public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (value.isDisabled())
                    return false;
                SoundManager.instance.playSound("ui-button-down");
                return true;
            }

//            @Override public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
//                if (value.isDisabled() || !value.isOver())
//                    return;
//
//            }
        });
        value.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                SoundManager.instance.playSound("ui-button-up");
            }
        });
    }

    public static void init(Actor value) {
        value.addListener(new InputListener() {
            @Override public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                SoundManager.instance.playSound("ui-button-down");
                return true;
            }

            @Override public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                SoundManager.instance.playSound("ui-button-up");
            }

        });
    }
}
