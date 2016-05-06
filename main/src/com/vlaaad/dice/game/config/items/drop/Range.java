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

package com.vlaaad.dice.game.config.items.drop;

import com.badlogic.gdx.math.MathUtils;

/**
 * Created 06.03.14 by vlaaad
 */
public class Range {

    private final int from;
    private final int to;

    /**
     * @param rangeDescriptor string like 1-100
     */
    public Range(String rangeDescriptor) {
        rangeDescriptor = rangeDescriptor.replaceAll("\\s", "");
        String[] parts = rangeDescriptor.split("-");
        if (parts.length != 2)
            throw new IllegalArgumentException(rangeDescriptor + " is not a valid descriptor for range!");
        from = Integer.parseInt(parts[0]);
        to = Integer.parseInt(parts[1]);
        if (from > to)
            throw new IllegalArgumentException("from > to: " + rangeDescriptor);
    }

    public Range(int from, int to) {
        this.from = from;
        this.to = to;
        if (from > to)
            throw new IllegalArgumentException("from > to: " + from + "-" + to);
    }

    public int getRandomInRange() {
        return MathUtils.random(from, to);
    }

    public boolean inRange(int value) {
        return value >= from && value <= to;
    }

    @Override public String toString() {
        return "(" + from + ", " + to + ')';
    }
}
