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

import com.badlogic.gdx.utils.Array;

/**
 * Created 06.03.14 by vlaaad
 */
public class Roll {
    private final Range range;
    private final Array<RangeItemCount> dropped;

    public Roll(Range range, Array<RangeItemCount> drop) {
        this.range = range;
        this.dropped = drop;
    }

    public Array<RangeItemCount> roll() {
        if (range == null) {
            return dropped;
        }
        Array<RangeItemCount> res = new Array<RangeItemCount>();
        int value = range.getRandomInRange();
        for (RangeItemCount rangeItemCount : dropped) {
            Range r = rangeItemCount.range;
            if (r == null || r.inRange(value)) {
                res.add(rangeItemCount);
            }
        }
        return res;
    }

    @Override public String toString() {
        return "range=" + range + ", items=" + dropped;
    }
}
