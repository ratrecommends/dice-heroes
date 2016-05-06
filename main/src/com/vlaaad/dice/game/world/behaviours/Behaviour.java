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

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.managers.SoundManager;

import java.util.Comparator;

/**
 * Created 08.10.13 by vlaaad
 */
public final class Behaviour {

    private static final Comparator<RequestProcessor> REQUEST_COMPARATOR = new Comparator<RequestProcessor>() {
        @Override public int compare(RequestProcessor o1, RequestProcessor o2) {
            return tmpMap.get(o2, 0) - tmpMap.get(o1, 0);
        }
    };

    private static final ObjectIntMap<RequestProcessor> tmpMap = new ObjectIntMap<RequestProcessor>();
    private static final Array<RequestProcessor> tmpList = new Array<RequestProcessor>();

    private final ObjectMap<BehaviourRequest, Array<RequestProcessor>> processors = new ObjectMap<BehaviourRequest, Array<RequestProcessor>>();

    public Behaviour() {
    }

    public <R, P> void registerProcessor(BehaviourRequest<R, P> request, RequestProcessor<R, P> processor) {
        processor.setRequest(request);
        Array<RequestProcessor> list = processors.get(request);
        if (list == null) {
            list = new Array<RequestProcessor>();
            processors.put(request, list);
        }
        list.add(processor);
    }


    @SuppressWarnings("unchecked")
    public <R, P> IFuture<R> request(BehaviourRequest<R, P> request, P params) {
        Array<RequestProcessor> list = processors.get(request);
        if (list == null)
            throw new IllegalStateException("Can't process request " + request + ": nothing registered");

        final ObjectIntMap<RequestProcessor> results = tmpMap;
        Array<RequestProcessor> fitting = tmpList;
        for (RequestProcessor processor : list) {
            int result = processor.preProcess(params);
            if (result >= 0) {
                results.put(processor, result);
                fitting.add(processor);
            }
        }
        if (fitting.size == 0)
            throw new IllegalStateException("Can't process request " + request + ", " + params + ": all processors failed: " + list);

        fitting.sort(REQUEST_COMPARATOR);
        IFuture<R> r = null;
        for (RequestProcessor p : fitting) {
            r = p.process(params);
            if (r != null)
                break;
        }
        if (r == null)
            throw new IllegalStateException("no processors returned result: " + request + ", " + params + ", " + list);

        tmpList.clear();
        tmpMap.clear();
        return r;
    }

}
