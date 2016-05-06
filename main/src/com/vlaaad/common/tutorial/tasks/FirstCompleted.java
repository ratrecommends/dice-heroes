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

import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.tutorial.Tutorial;
import com.vlaaad.common.tutorial.TutorialTask;
import com.vlaaad.common.util.futures.IFutureListener;

/**
 * Created 15.11.13 by vlaaad
 */
public class FirstCompleted extends TutorialTask {
    private final Array<TutorialTask>[] tasksArrays;
    private final Array<Tutorial> tutorials;
    private Callback callback;

    public FirstCompleted(Array<TutorialTask>... tasksArrays) {
        super();
        this.tasksArrays = tasksArrays;
        this.tutorials = new Array<Tutorial>(tasksArrays.length);
    }

    @Override public void start(Callback callback) {
        this.callback = callback;
        for (Array<TutorialTask> tasksArray : tasksArrays) {
            Tutorial tutorial = new Tutorial(resources, tasksArray);
            tutorials.add(tutorial);
            tutorial.start().addListener(listener);
        }
    }

    private IFutureListener<Tutorial> listener = new IFutureListener<Tutorial>() {
        @Override public void onHappened(Tutorial tutorial) {
            for (Tutorial check : tutorials) {
                if (check == tutorial)
                    continue;
                check.cancel();
            }
            callback.taskEnded();
        }
    };
}
