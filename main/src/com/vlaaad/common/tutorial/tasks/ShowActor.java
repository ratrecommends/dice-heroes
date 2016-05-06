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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.vlaaad.common.tutorial.TutorialTask;

/**
 * Created 16.11.13 by vlaaad
 */
public abstract class ShowActor extends TutorialTask {

    private final String targetResourceName;

    public ShowActor(String targetResourceName) {
        this.targetResourceName = targetResourceName;
    }

    @Override public void start(Callback callback) {
        Actor actor = getActorToShow();
        Group target = getTarget();

        target.addActor(actor);
        resources.put(targetResourceName, actor);
        callback.taskEnded();
    }

    protected abstract Group getTarget();

    protected abstract Actor getActorToShow();
}
