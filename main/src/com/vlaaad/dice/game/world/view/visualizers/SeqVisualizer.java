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

package com.vlaaad.dice.game.world.view.visualizers;

import com.vlaaad.common.util.CountDown;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.game.world.view.IVisualizer;

public class SeqVisualizer<T> implements IVisualizer<T> {

    private final IVisualizer<? super T>[] visualizers;

    private SeqVisualizer(IVisualizer<? super T>... visualizers) {
        this.visualizers = visualizers;
    }

    public static <T> SeqVisualizer<T> make(IVisualizer<? super T>... visualizers) {
        return new SeqVisualizer<T>(visualizers);
    }

    @Override public IFuture<Void> visualize(T t) {
        final Future<Void> future = Future.make();
        CountDown countDown = new CountDown(visualizers.length, future);
        if (!countDown.isDone()) {
            next(0, countDown, visualizers, t);
        }
        return future;
    }

    private void next(final int index, final CountDown countDown, final IVisualizer<? super T>[] visualizers, final T t) {
        visualizers[index].visualize(t).addListener(new IFutureListener<Void>() {
            @Override public void onHappened(Void aVoid) {
                if (!countDown.tick()) {
                    next(index + 1, countDown, visualizers, t);
                }
            }
        });
    }


}
