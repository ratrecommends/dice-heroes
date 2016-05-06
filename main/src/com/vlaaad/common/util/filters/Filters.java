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

import com.vlaaad.common.util.filters.imp.EqFilter;
import com.vlaaad.common.util.filters.imp.ExistsFilter;
import com.vlaaad.common.util.filters.imp.ForallFilter;

/**
 * Created 18.05.14 by vlaaad
 */
public class Filters {
    public static <T> EqFilter<T> eq(T t) {
        return new EqFilter<T>(t);
    }

    public static <T> ExistsFilter<T> exists(Filter<T> filter) {
        return new ExistsFilter<T>(filter);
    }

    public static <T> ForallFilter<T> forall(Filter<T> filter) {
        return new ForallFilter<T>(filter);
    }
}
