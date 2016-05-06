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

package com.vlaaad.common.util.signals;

import com.badlogic.gdx.utils.SnapshotArray;

/**
 * Created 10.10.13 by vlaaad
 */
public class Signal<T> {
    private final SnapshotArray<ISignalListener<T>> listeners = new SnapshotArray<ISignalListener<T>>(ISignalListener.class);

    public void add(ISignalListener<T> listener) {
        if (!listeners.contains(listener, true))
            listeners.add(listener);
    }

    public void remove(ISignalListener<T> listener) {
        listeners.removeValue(listener, true);
    }

    public void dispatch(T t) {
        final ISignalListener<T>[] items = listeners.begin();
        for (int i = 0, n = listeners.size; i < n; i++) {
            items[i].handle(t);
        }
        listeners.end();
    }
}
