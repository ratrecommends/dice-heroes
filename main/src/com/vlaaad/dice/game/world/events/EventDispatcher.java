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

package com.vlaaad.dice.game.world.events;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.SnapshotArray;

/**
 * Created 07.10.13 by vlaaad
 */
public class EventDispatcher {
    private ObjectMap<EventType, SnapshotArray> data
        = new ObjectMap<EventType, SnapshotArray>();

    public <T> void add(EventType<T> type, EventListener<T> listener) {
        SnapshotArray<EventListener<T>> list = getList(type, true);
        list.add(listener);
    }

    public <T> void remove(EventType<T> type, EventListener<T> listener) {
        SnapshotArray<EventListener<T>> list = getList(type, false);
        if (list == null)
            return;
        list.removeValue(listener, true);
    }

    public <T> void dispatch(EventType<T> type, T t) {
        SnapshotArray<EventListener<T>> list = getList(type, false);
        if (list == null)
            return;
        EventListener<T>[] items = list.begin();
        for (int i = 0, n = list.size; i < n; i++) {
            items[i].handle(type, t);
        }
        list.end();
    }

    @SuppressWarnings("unchecked")
    private <T> SnapshotArray<EventListener<T>> getList(EventType<T> type, boolean createIfNotExist) {
        Object list = data.get(type);
        if (list == null) {
            if (!createIfNotExist)
                return null;
            SnapshotArray<EventListener<T>> result = new SnapshotArray<EventListener<T>>(EventListener.class);
            data.put(type, result);
            return result;
        }
        return (SnapshotArray<EventListener<T>>) list;
    }
}
