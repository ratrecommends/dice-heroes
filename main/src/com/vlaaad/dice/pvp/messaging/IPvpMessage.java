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

package com.vlaaad.dice.pvp.messaging;

import java.util.Comparator;

/**
 * Created 28.07.14 by vlaaad - marker interface for all pvp messages
 */
public abstract class IPvpMessage {
    public static final Comparator<? super IPvpMessage> COMPARATOR = new Comparator<IPvpMessage>() {
        @Override public int compare(IPvpMessage o1, IPvpMessage o2) {
            return o2.packetIdx - o1.packetIdx;
        }
    };
    public int packetIdx = -1;
}
