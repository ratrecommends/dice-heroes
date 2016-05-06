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

import com.badlogic.gdx.utils.Pool;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created 03.08.13 by vlaaad
 */
public class Grid2D<T> {

    public Set<Map.Entry<Coordinate, T>> entries() {
        return data.entrySet();
    }

    public static Coordinate obtain(int x, int y) {
        Grid2D.Coordinate res = pool.obtain();
        res.set(x, y);
        return res;
    }

    public static void free(Coordinate coordinate) {
        pool.free(coordinate);
    }

    public static interface ICell {
        public void setPosition(int x, int y);
    }

    private static Pool<Coordinate> pool = new Pool<Coordinate>() {
        @Override
        protected Coordinate newObject() {
            return new Coordinate();
        }
    };

    public static class Coordinate {
        private int x;

        private int y;

        public int x() { return x; }

        public int y() { return y; }

        public Coordinate() { }

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        private Coordinate set(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        @Override
        public int hashCode() { return (31 + x) * 31 + y; }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Coordinate) {
                Coordinate c = (Coordinate) obj;
                return x == c.x && y == c.y;
            }
            return false;
        }

        @Override public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }

    public Grid2D() {
    }

    @Override public String toString() {
        return data.toString();
    }

    public void translateAll(int dx, int dy) {
        if (tmp == null)
            tmp = new HashMap<Coordinate, T>();
        for (Coordinate coordinate : keys()) {
            T value = data.get(coordinate);
            coordinate.x += dx;
            coordinate.y += dy;
            tmp.put(coordinate, value);
        }
        data.clear();
        data.putAll(tmp);
        tmp.clear();
    }

    private Map<Coordinate, T> tmp;

    private final Map<Coordinate, T> data = new HashMap<Coordinate, T>();

    public int size() { return data.size(); }

    public boolean isEmpty() { return data.isEmpty(); }

    public boolean hasAt(int x, int y) {
        Coordinate c = pool.obtain().set(x, y);
        boolean result = data.containsKey(c);
        pool.free(c);
        return result;
    }

    public boolean has(T value) { return data.containsValue(value); }

    public T get(Coordinate coordinate) {
        return data.get(coordinate);
    }

    public T get(int x, int y) {
        Coordinate c = pool.obtain().set(x, y);
        T result = data.get(c);
        pool.free(c);
        return result;
    }

    public T put(int x, int y, T value) {
        Coordinate c = pool.obtain().set(x, y);
        if (value instanceof ICell) {
            ((ICell) value).setPosition(x, y);
        }
        return data.put(c, value);
    }

    public T remove(int x, int y) {
        Coordinate c = pool.obtain().set(x, y);
        T result = data.remove(c);
        pool.free(c);
        return result;
    }

    public void clear() {
        for (Coordinate c : data.keySet()) {
            pool.free(c);
        }
        data.clear();
    }

    public Set<Coordinate> keys() {
        return data.keySet();
    }

    public Collection<T> values() {
        return data.values();
    }

}
