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

/**
 * Created 07.11.13 by vlaaad
 */
public abstract class TutorialTask {

    protected Tutorial tutorial;
    protected Tutorial.TutorialResources resources;

    public void initialize(Tutorial tutorial, Tutorial.TutorialResources resources) {
        this.tutorial = tutorial;
        this.resources = resources;
    }

    public static interface Callback {
        public void taskEnded();
    }

    public abstract void start(Callback callback);

    public void cancel() {
        throw new UnsupportedOperationException("can't cancel " + this + ": not implemented");
    }
}
