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
 * Created 17.11.13 by vlaaad
 */
public class Looped extends TutorialTask {
    private final Array<TutorialTask> tasks;
    private Tutorial current;
    private String tutorialName;

    public Looped(TutorialTask... tasks) {
        this(Array.with(tasks));
    }

    public Looped(Array<TutorialTask> tasks) {
        super();
        this.tasks = tasks;
    }

    @Override public void start(Callback callback) {
        startTutorial();
        //never ending
    }

    private void startTutorial() {
        current = new Tutorial(tutorialName == null ? "unnamed" : tutorialName, resources, new Array<TutorialTask>(tasks));
        current.start().addListener(new IFutureListener<Tutorial>() {
            @Override public void onHappened(Tutorial tutorial) {
                startTutorial();
            }
        });
    }

    @Override public void cancel() {
        if (current != null)
            current.cancel();
    }

    public Looped withName(String tutorialName) {
        this.tutorialName = tutorialName;
        return this;
    }
}
