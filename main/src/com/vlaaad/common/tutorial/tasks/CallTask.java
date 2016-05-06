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

import com.vlaaad.common.tutorial.Tutorial;
import com.vlaaad.common.tutorial.TutorialTask;

/**
 * Created 07.11.13 by vlaaad
 */
public class CallTask extends TutorialTask {

    private final Callable callable;

    public CallTask(Callable callable) {
        this.callable = callable;
    }

    @Override public void start(Callback callback) {
        callable.call(tutorial, resources);
        callback.taskEnded();
    }

    public static interface Callable {

        void call(Tutorial tutorial, Tutorial.TutorialResources resources);
    }
}
