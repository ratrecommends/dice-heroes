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

package com.vlaaad.dice.game.world.behaviours;

import com.vlaaad.common.util.futures.IFuture;

/**
 * Created 14.01.14 by vlaaad
 */
public abstract class RequestProcessor<R, P> {

    protected BehaviourRequest<R, P> request;
    /**
     * @param params
     * @return priority, -1 if can't process
     */
    public abstract int preProcess(P params);

    public abstract IFuture<R> process(P params);

    public void setRequest(BehaviourRequest<R, P> request) {
        this.request = request;
    }

    /**
     * option to cancel any current listeners
     */
    public void cancel() {}
}
