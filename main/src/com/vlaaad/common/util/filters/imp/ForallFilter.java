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

package com.vlaaad.common.util.filters.imp;

import com.vlaaad.common.util.filters.Filter;

/**
 * Created 20.05.14 by vlaaad
 */
public class ForallFilter<T> implements Filter<Iterable<T>> {
    private final Filter<T> filter;

    public ForallFilter(Filter<T> filter) {
        this.filter = filter;
    }

    @Override public boolean accept(Iterable<T> iterable) {
        for (T t : iterable) {
            if (!filter.accept(t))
                return false;
        }
        return true;
    }
}
