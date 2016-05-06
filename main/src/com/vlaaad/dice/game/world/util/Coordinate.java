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

package com.vlaaad.dice.game.world.util;

import com.badlogic.gdx.utils.Pool;

/**
 * Created 19.11.13 by vlaaad
 */
public class Coordinate implements Pool.Poolable {
    private static final Pool<Coordinate> COORDINATE_POOL = new Pool<Coordinate>() {
        @Override protected Coordinate newObject() {
            return new Coordinate();
        }
    };

    public static Coordinate obtain(int x, int y) {
        return COORDINATE_POOL.obtain().set(x, y);
    }


    public static void freeAll(Iterable<Coordinate> source) {
        for (Coordinate coordinate : source) {
            coordinate.free();
        }
    }

    private boolean free;
    public int x;
    public int y;

    private Coordinate() {
    }

    public Coordinate set(int x, int y) {
        this.x = x;
        this.y = y;
        this.free = false;
        return this;
    }

    @Override
    public String toString() {
        return "{" + x + ", " + y + "}";
    }

    @Override public int hashCode() {
        return x * 31 + y;
    }

    public void free() {
        if (!free) {
            COORDINATE_POOL.free(this);
        }
    }

    @Override public boolean equals(Object obj) {
        if (obj instanceof Coordinate) {
            Coordinate c = (Coordinate) obj;
            return c.x == x && c.y == y;
        }
        return false;
    }

    @Override public void reset() {
        free = true;
    }
}
