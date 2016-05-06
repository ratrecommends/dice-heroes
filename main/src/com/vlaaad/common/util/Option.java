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

/**
 * Created 25.07.14 by vlaaad
 */
public abstract class Option<T> {

    @SuppressWarnings("unchecked")
    public static <A> Option<A> option(A value) {
        return value == null ? none : some(value);
    }

    public static <A> Option<A> some(final A value) {
        return new Option<A>() {
            @Override public boolean isDefined() { return true; }
            @Override public A get() { return value; }
            @Override public String toString() { return "Some(" + value + ")"; }
        };
    }


    @SuppressWarnings("unchecked")
    public static <A> Option<A> none() {
        return none;
    }

    @SuppressWarnings("unchecked")
    public static <A> Option<A> none(Class<A> type) {
        return none;
    }

    private static final Option none = new Option() {
        @Override public boolean isDefined() { return false; }
        @Override public Object get() { throw new IllegalStateException("none.get"); }
        @Override public String toString() { return "None()"; }
    };

    private Option() {
    }


    public final boolean isEmpty() {
        return !isDefined();
    }
    public abstract boolean isDefined();
    public abstract T get();
}
