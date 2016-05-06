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

package com.vlaaad.dice.game.world.behaviours.processors.pvp;

import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.world.behaviours.RequestProcessor;
import com.vlaaad.dice.states.PvpPlayState;

/**
 * Created 29.07.14 by vlaaad
 */
public class PvpOtherProcessor<Response, Params> extends RequestProcessor<Response, Params> {

    private final PvpPlayState state;

    public PvpOtherProcessor(PvpPlayState state) {
        super();
        this.state = state;
    }

    @Override public int preProcess(Params params) {
        return 1;
    }

    @Override public IFuture<Response> process(Params params) {
//        Future<Response> future = new Future<Response>();
//        state.waitForRoundMessage(future);
        return state.waitForRoundMessage();
    }
}
