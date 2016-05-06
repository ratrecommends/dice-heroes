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

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.vlaaad.common.util.Consumer;
import com.vlaaad.dice.Config;

public class CountDownLabel extends Label {

    private float remainingTime;
    private Consumer<CountDownLabel> callback;
    private int remainingSeconds;
    private boolean notified;

    public CountDownLabel(float remainingTime, Consumer<CountDownLabel> callback) {
        super(String.valueOf((int) remainingTime), Config.skin);
        this.remainingTime = remainingTime;
        this.callback = callback;
        this.remainingSeconds = (int) remainingTime;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        remainingTime -= delta;
        int seconds = (int) remainingTime;
        if (seconds != remainingSeconds) {
            remainingSeconds = seconds;
            setText(String.valueOf(seconds));
        }
        if (!notified && remainingTime <= 0) {
            notified = true;
            callback.consume(this);
        }
    }
}
