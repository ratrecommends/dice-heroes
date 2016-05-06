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

package com.vlaaad.dice.game.world.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.vlaaad.dice.Config;

/**
 * Created 07.10.13 by vlaaad
 */
public class ImageSubView implements SubView {

    private final Actor image;
    private final int priority;

    public ImageSubView(String name) {
        this(name, 0);
    }

    public ImageSubView(String name, int priority) {
        this.priority = priority;
        image = new Image(Config.skin, name);
    }

    @Override public int getPriority() {
        return priority;
    }

    @Override public void play(String animationName) {

    }

    @Override public Actor getActor() {
        return image;
    }
}
