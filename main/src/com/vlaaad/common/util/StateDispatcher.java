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

import com.badlogic.gdx.utils.SnapshotArray;

/**
 * Created 18.05.14 by vlaaad
 */
public class StateDispatcher<T> implements IStateDispatcher<T> {

    private T state;
    private SnapshotArray<Listener<T>> listeners = new SnapshotArray<Listener<T>>(Listener.class);

    public StateDispatcher(T initialState) {
        state = initialState;
    }

    @Override public void addListener(Listener<T> listener, boolean notifyImmediately) {
        if (!listeners.contains(listener, true))
            listeners.add(listener);
        if (notifyImmediately) listener.onChangedState(state);
    }

    @Override public void removeListener(Listener<T> listener) {
        listeners.removeValue(listener, true);
    }

    @Override public void clearListeners() {
        listeners.clear();
    }

    @Override public T getState() {
        return state;
    }

    @SuppressWarnings("unchecked")
    public boolean setState(T t) {
        if (t == null) throw new IllegalArgumentException("nulls are not allowed here!");
        if (state == t || (state != null && state.equals(t)))
            return false;
        state = t;
        Listener[] items = listeners.begin();
        for (int i = 0, n = listeners.size; i < n; i++) {
            items[i].onChangedState(t);
        }
        listeners.end();
        return true;
    }

}
