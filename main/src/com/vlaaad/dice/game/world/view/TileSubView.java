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

/**
 * Created 10.01.14 by vlaaad
 */
public class TileSubView implements SubView {

    private final Actor tile;
    private final int priority;

    public TileSubView(String drawableName) {
        this(drawableName, 0);
    }

    public TileSubView(String drawableName, int priority) {
        this.tile = new Tile(drawableName);
        this.priority = priority;
    }


    @Override public int getPriority() {
        return priority;
    }

    @Override public void play(String animationName) {
    }

    @Override public Actor getActor() {
        return tile;
    }
}
