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

package com.vlaaad.common.util.filters;

import com.badlogic.gdx.utils.Array;

/**
 * Created 16.05.14 by vlaaad
 */
public class AllOfFilter<T> implements Filter<T> {

    private final Array<Filter<T>> children;

    public AllOfFilter() {
        this(new Array<Filter<T>>());
    }

    public AllOfFilter(Array<Filter<T>> filters) {
        children = filters;
    }

    public void add(Filter<T> filter) {
        if (!children.contains(filter, true)) children.add(filter);
    }

    public void remove(Filter<T> filter) {
        children.removeValue(filter, true);
    }

    @Override public boolean accept(T t) {
        if (children.size == 0)
            throw new IllegalStateException("can't accept: no sub filters");
        for (Filter<T> filter : children) {
            if (!filter.accept(t))
                return false;
        }
        return true;
    }
}
