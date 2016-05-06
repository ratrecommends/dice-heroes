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

package com.vlaaad.common.util;

import com.vlaaad.common.util.futures.IFutureListener;

/**
 * Created 06.03.14 by vlaaad
 */
public class CountDown implements Runnable, IFutureListener<Void> {
    private int count;
    private final Runnable runnable;

    public CountDown(int count, Runnable runnable) {
        if (count < 0)
            throw new IllegalArgumentException("count must be >= 1");
        this.count = count;
        this.runnable = runnable;
        if (count == 0) runnable.run();
    }

    public boolean tick() {
        count--;
        if (count == 0) {
            runnable.run();
            return true;
        } else if (count < 0) {
            Logger.error("counted down too mush!");
            return true;
        }
        return false;
    }

    public boolean isDone() {
        return count <= 0;
    }

    @Override public void run() {
        tick();
    }

    @Override public void onHappened(Void aVoid) {
        tick();
    }
}
