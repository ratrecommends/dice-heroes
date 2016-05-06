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

package com.vlaaad.common.util.futures;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.vlaaad.common.util.Logger;

/**
 * Created 07.10.13 by vlaaad
 */
public class Future<T> implements IFuture<T>, IFutureListener<T>, Runnable {

    private static final IFuture nullCompleted = new IFuture() {
        @Override @SuppressWarnings("unchecked")public IFuture addListener(IFutureListener listener) {
            listener.onHappened(null);
            return this;
        }
        @Override public boolean isHappened() { return true; }
    };

    public static <T> IFuture<T> completed(final T v) {
        return new IFuture<T>() {
            @Override public IFuture<T> addListener(IFutureListener<? super T> listener) {
                listener.onHappened(v);
                return this;
            }
            @Override public boolean isHappened() { return true; }
        };
    }

    @SuppressWarnings("unchecked") public static <T> IFuture<T> completed() {
        return nullCompleted;
    }

    private boolean happened;
    private Array<IFutureListener<? super T>> listeners;
    private T result;

    public void happen() {
        happen(null);
    }

    public void reset() {
        happened = false;
        listeners = null;
        result = null;
    }

    public void happen(T t) {
        if (happened) {
            Logger.error("future already happened", new RuntimeException());
        }
        happened = true;
        this.result = t;
        if (listeners == null)
            return;
        Array<IFutureListener<? super T>> listeners = this.listeners;
        this.listeners = null;
        for (IFutureListener<? super T> listener : listeners) {
            listener.onHappened(result);
        }
    }

    @Override public IFuture<T> addListener(IFutureListener<? super T> listener) {
        if (happened) {
            listener.onHappened(result);
        } else {
            if (listeners == null) {
                listeners = new Array<IFutureListener<? super T>>(1);
            }
            listeners.add(listener);
        }
        return this;
    }

    public boolean isHappened() {
        return happened;
    }

    @Override public void onHappened(T t) {
        happen(t);
    }

    @Override public void run() {
        happen();
    }

    public static <T> Future<T> make() {
        return new Future<T>();
    }
}
