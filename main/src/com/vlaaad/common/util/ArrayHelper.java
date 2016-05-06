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

import com.badlogic.gdx.utils.Array;

public class ArrayHelper {
    public static <T> Array<T> from(Iterable<? extends T> iterable) {
        final Array<T> result = new Array<T>();
        for (T t : iterable) {
            result.add(t);
        }
        return result;
    }

    public static <O, I> Array<O> map(Iterable<? extends I> iterable, Function<I, O> mapper) {
        final Array<O> result = new Array<O>();
        for (I i : iterable) {
            result.add(mapper.apply(i));
        }
        return result;
    }
}
